package com.example.domain.qibla

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Encapsulates the live state of device orientation sensors.
 */
data class CompassSensorState(
    val isSensorAvailable: Boolean = false,
    val sensorName: String = "",
    val trueHeading: Float = 0f, // 0 to 360 degrees relative to True Geographic North
    val magneticHeading: Float = 0f, // 0 to 360 degrees relative to Magnetic North
    val declination: Float = 0f, // Magnetic declination in degrees
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val pitch: Float = 0f, // Device pitch angle (-90 to +90 degrees)
    val roll: Float = 0f, // Device roll angle (-180 to +180 degrees)
    val isDeviceFlat: Boolean = true // True if phone tilt is within 25 degrees of horizontal
)

/**
 * Hardware sensor manager integrating Android Sensor API (Rotation Vector, Accelerometer, Magnetometer)
 * with automatic True North declination compensation, circular smoothing, and flat-surface detection.
 */
class QiblaSensorManager(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val hasCompassSensors: Boolean
        get() = rotationVectorSensor != null || (accelerometer != null && magnetometer != null)

    /**
     * Emits continuous real-time compass updates as a Kotlin Flow with automatic lifecycle management.
     */
    fun getCompassOrientationFlow(
        userLat: Double = 0.0,
        userLng: Double = 0.0,
        userAlt: Double = 0.0
    ): Flow<CompassSensorState> = callbackFlow {
        if (sensorManager == null || !hasCompassSensors) {
            trySend(
                CompassSensorState(
                    isSensorAvailable = false,
                    sensorName = "No magnetic hardware sensor available"
                )
            )
            awaitClose { }
            return@callbackFlow
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false

        // Circular exponential smoothing state
        var smoothedSin = 0.0
        var smoothedCos = 0.0
        var isFirstReading = true
        val smoothingFactor = 0.18 // Low-pass filter weight

        // Calculate magnetic declination for true geographic north alignment
        val declination: Float = if (userLat != 0.0 || userLng != 0.0) {
            try {
                val geoField = GeomagneticField(
                    userLat.toFloat(),
                    userLng.toFloat(),
                    userAlt.toFloat(),
                    System.currentTimeMillis()
                )
                geoField.declination
            } catch (e: Exception) {
                0f
            }
        } else {
            0f
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var azimuthRad = 0f
                var pitchRad = 0f
                var rollRad = 0f
                var currentAccuracy = event.accuracy

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    azimuthRad = orientation[0]
                    pitchRad = orientation[1]
                    rollRad = orientation[2]
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, gravity, 0, 3)
                    hasGravity = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                    hasGeomagnetic = true
                }

                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR && hasGravity && hasGeomagnetic) {
                    val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
                    if (success) {
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        azimuthRad = orientation[0]
                        pitchRad = orientation[1]
                        rollRad = orientation[2]
                    } else {
                        return
                    }
                } else if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) {
                    return
                }

                // Raw magnetic azimuth in degrees [0, 360)
                val rawMagAzimuthDeg = ((Math.toDegrees(azimuthRad.toDouble()) + 360.0) % 360.0)

                // Circular smoothing to prevent needle jitter and 359° <-> 0° boundary snapping
                val rad = Math.toRadians(rawMagAzimuthDeg)
                val currentSin = sin(rad)
                val currentCos = cos(rad)

                val smoothMagDeg: Float = if (isFirstReading) {
                    smoothedSin = currentSin
                    smoothedCos = currentCos
                    isFirstReading = false
                    rawMagAzimuthDeg.toFloat()
                } else {
                    smoothedSin = (smoothingFactor * currentSin) + ((1.0 - smoothingFactor) * smoothedSin)
                    smoothedCos = (smoothingFactor * currentCos) + ((1.0 - smoothingFactor) * smoothedCos)
                    val smoothedRad = atan2(smoothedSin, smoothedCos)
                    ((Math.toDegrees(smoothedRad) + 360.0) % 360.0).toFloat()
                }

                // True North calculation by adding geographic declination
                val trueHeading = ((smoothMagDeg + declination + 360f) % 360f)

                // Pitch and roll in degrees
                val pitchDeg = Math.toDegrees(pitchRad.toDouble()).toFloat()
                val rollDeg = Math.toDegrees(rollRad.toDouble()).toFloat()
                val isFlat = kotlin.math.abs(pitchDeg) < 25f && kotlin.math.abs(rollDeg) < 25f

                val state = CompassSensorState(
                    isSensorAvailable = true,
                    sensorName = event.sensor.name,
                    trueHeading = trueHeading,
                    magneticHeading = smoothMagDeg,
                    declination = declination,
                    accuracy = currentAccuracy,
                    pitch = pitchDeg,
                    roll = rollDeg,
                    isDeviceFlat = isFlat
                )

                trySend(state)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Handled in onSensorChanged or available when calibration changes
            }
        }

        val sensorRegistered = if (rotationVectorSensor != null) {
            sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            val accOk = sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            val magOk = sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
            accOk && magOk
        }

        if (!sensorRegistered) {
            trySend(
                CompassSensorState(
                    isSensorAvailable = false,
                    sensorName = "Failed to register sensor listeners"
                )
            )
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
