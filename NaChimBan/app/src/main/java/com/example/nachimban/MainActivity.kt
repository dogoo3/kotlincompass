package com.example.nachimban

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    var sensorManager : SensorManager? = null
    var mGravitymeter : Sensor? = null
    var mAccelerometer : Sensor? = null
    var mMagnetometer : Sensor? = null
    var mVectormeter : Sensor? = null
    var mOrientCount: Int = 0
    var mTxtOrient: TextView? = null

    var mGravity = FloatArray(3)
    var mAccelar = FloatArray(3)
    var mMagnetic = FloatArray(3)
    var mVector = FloatArray(5)

    var mR = FloatArray(9)
    var mI = FloatArray(9)
    var mV = FloatArray(9)

    val FREQ = 1

    var r = 0.0

    val dumpValues = {v : FloatArray -> String.format("%.6f, %.6f, %.6f", v[0], v[1], v[2])}
    val dumpMatrix = {m : FloatArray ->
        String.format(
            "%.6f, %.6f, %.6f\n%.6f, %.6f, %.6f\n%.6f, %.6f, %.6f\n",
            m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]
        )}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTxtOrient = findViewById(R.id.result)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mGravitymeter = sensorManager!!.getDefaultSensor(Sensor.TYPE_GRAVITY)
        mAccelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mMagnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        mVectormeter = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this@MainActivity, mGravitymeter, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager!!.registerListener(this@MainActivity, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager!!.registerListener(this@MainActivity, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager!!.registerListener(this@MainActivity, mVectormeter, SensorManager.SENSOR_DELAY_NORMAL)
    }


    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this@MainActivity, mGravitymeter)
        sensorManager!!.unregisterListener(this@MainActivity, mAccelerometer)
        sensorManager!!.unregisterListener(this@MainActivity, mMagnetometer)
        sensorManager!!.unregisterListener(this@MainActivity, mVectormeter)
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0 != null) {
            if(p0.sensor.type == Sensor.TYPE_GRAVITY) {
                System.arraycopy(p0.values, 0, mGravity, 0, p0.values.size)
            }
            if(p0.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(p0.values, 0, mAccelar, 0, p0.values.size)
            }
            if(p0.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(p0.values, 0, mMagnetic, 0, p0.values.size)
            }

            if(mAccelar != null && mMagnetic != null && mGravity != null) {
                SensorManager.getRotationMatrix(mR, mI, mAccelar, mMagnetic)

                var inclination = SensorManager.getInclination(mI)
                SensorManager.getOrientation(mR, mV)

                val setaZ = atan2(mR[7], mR[8])

                mTxtOrient!!.text =
                    "Acc : ${dumpValues(mAccelar)}\n" +
                    "Mag : ${dumpValues(mMagnetic)}\n" +
                    "R : \n${dumpMatrix(mR)}\n" +
                    "I : \n${dumpMatrix(mI)}\n" +
                    "inclination : $inclination\n" +
                    "Rot : \n${dumpMatrix(mV)}\n" +
                    "Top : \n" +
                    "x : ${String.format("%.6f", cos(mV[0]) * cos(mV[1]))}\n" +
                    "y : ${String.format("%.6f", sin(mV[0]) * cos(mV[1]))}\n" +
                    "z : ${String.format("%.6f", sin(mV[1]-PI/2))}\n" +
                    "Left : \n" +
                    "x : ${String.format("%.6f", -cos(mV[0])*sin(mV[1])*sin(mV[2]) - sin(mV[0])*cos(mV[2]))}\n" +
                    "y : ${String.format("%.6f", -sin(mV[0])*sin(mV[1])*sin(mV[2]) + cos(mV[0])*cos(mV[2]))}\n" +
                    "z : ${String.format("%.6f", cos(mV[1])*sin(mV[2]))}\n" +
                    "Back : \n" +
                    "x : ${String.format("%.6f", -cos(mV[0])*sin(mV[1])*cos(mV[2]) + sin(mV[0])*sin(mV[2]))}\n" +
                    "y : ${String.format("%.6f", -sin(mV[0])*sin(mV[1])*cos(mV[2]) - cos(mV[0])*sin(mV[2]))}\n" +
                    "z : ${String.format("%.6f", cos(mV[1])*sin(mV[2]-PI/2))}\n" +
                    "Angle : \n" +
                    "x : ${String.format("%.6f", atan2(mR[7], mR[8]))}\n" +
                    "y : ${String.format("%.6f", atan2(-mR[6], sqrt(mR[7] * mR[7] + mR[8] * mR[8])))}\n" +
                    "z : ${String.format("%.6f", atan2(mR[3], mR[0]))}\n"+
                    "Gravity : \n" +
                    "x : ${String.format("%.6f", mGravity[0] * 9)}\n" +
                    "y : ${String.format("%.6f", mGravity[1] * 9)}\n" +
                    "z : ${String.format("%.6f", mGravity[2] * 9)}\n"

            }
        }
    }


    //| cos(yaw)cos(pitch) -cos(yaw)sin(pitch)sin(roll)-sin(yaw)cos(roll) -cos(yaw)sin(pitch)cos(roll)+sin(yaw)sin(roll)|
    //| sin(yaw)cos(pitch) -sin(yaw)sin(pitch)sin(roll)+cos(yaw)cos(roll) -sin(yaw)sin(pitch)cos(roll)-cos(yaw)sin(roll)|
    //| sin(pitch)          cos(pitch)sin(roll)                            cos(pitch)sin(roll)                          |

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
    // --------------------------------------------------------------------------
//    var mSensorManager : SensorManager? = null
//    var mAccelerometer : Sensor? = null
//    var mGravitymeter : Sensor? = null
//    var mMagnetometer : Sensor? = null
//    var mVectormeter : Sensor? = null
//    var mLastAccelerometer = FloatArray(3)
//    var mLastMagnetometer = FloatArray(3)
//    var mLastVectormeter = FloatArray(5)
//    var mLastAccelerometerSet : Boolean = false
//    var mLastMagnetometerSet : Boolean = false
//    var mLastVectorSet : Boolean = false
//    var mLastGravitySet : Boolean = false
//    var mR = FloatArray(9)
//    var mVR = FloatArray(9)
//    var mI = FloatArray(9)
//    var mOrientation = FloatArray(3)
//    var mLastGravitymeter = FloatArray(3)
//    var mCurrentDegree = 0.0f
//    var mPointer : ImageView? = null
//    var DegreeTV : TextView? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        mMagnetometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
//        mVectormeter = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
//        mGravitymeter = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GRAVITY)
//        DegreeTV = findViewById(R.id.degreeTV)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mSensorManager!!.registerListener(this@MainActivity, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
//        mSensorManager!!.registerListener(this@MainActivity, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL)
//        mSensorManager!!.registerListener(this@MainActivity, mVectormeter, SensorManager.SENSOR_DELAY_NORMAL)
//        mSensorManager!!.registerListener(this@MainActivity, mGravitymeter, SensorManager.SENSOR_DELAY_NORMAL)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mSensorManager!!.unregisterListener(this@MainActivity, mAccelerometer)
//        mSensorManager!!.unregisterListener(this@MainActivity, mMagnetometer)
//        mSensorManager!!.unregisterListener(this@MainActivity, mVectormeter)
//        mSensorManager!!.unregisterListener(this@MainActivity, mGravitymeter)
//    }
//
//    override fun onSensorChanged(p0: SensorEvent?) {
//        if (p0 != null) {
//            if (p0.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//                System.arraycopy(p0.values, 0, mLastAccelerometer, 0, p0.values.size)
//                mLastAccelerometerSet = true
//            } else if (p0.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
//                System.arraycopy(p0.values, 0, mLastMagnetometer, 0, p0.values.size)
//                mLastMagnetometerSet = true
//            } else if (p0.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
//                System.arraycopy(p0.values, 0, mLastVectormeter, 0, p0.values.size)
//                mLastVectorSet = true
//            } else if (p0.sensor.type == Sensor.TYPE_GRAVITY) {
//                System.arraycopy(p0.values, 0, mLastGravitymeter, 0, p0.values.size)
//                mLastGravitySet = true
//            }
//
//            if (mLastAccelerometerSet && mLastMagnetometerSet) {
//                var R = FloatArray(9)
//
//                if (SensorManager.getRotationMatrix(R, null, mLastAccelerometer, mLastMagnetometer)) {
//                    SensorManager.getOrientation(R, mOrientation)
//                    val azimuthDegree = mOrientation[0] * 180 / PI
//                    DegreeTV!!.text = azimuthDegree.toString()
//                    findViewById<TextView>(com.example.nachimban.R.id.a).text = mOrientation[0].toString()
//                    findViewById<TextView>(com.example.nachimban.R.id.b).text = mOrientation[1].toString()
//                    findViewById<TextView>(com.example.nachimban.R.id.c).text = mOrientation[2].toString()
//                }
//            }
//
////            if(mLastGravitySet && mLastMagnetometerSet) {
////                if(SensorManager.getRotationMatrix(mR, null, mLastGravitymeter, mLastMagnetometer)) {
////                    DegreeTV!!.text = acos(mR[8]).toString()
////                }
////            }
//
//            if (mLastVectorSet) {
////                SensorManager.getRotationMatrixFromVector(mVR, p0.values)
////                // val asdf = atan2(-mVR[2].toDouble(), -mVR[5].toDouble())
////                var angle = atan((mVR!![1] - mVR!![3]) / (mVR!![0] + mVR!![4]))
////                angle *= 114.59156f
////                angle = (angle + 360) % 360
////                DegreeTV!!.text = angle.toString()
////                mLastVectorSet = false
//            }
//        }
//    }
//
//
//
//    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//    }
}