package com.narcoding.localnotepad.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.SparseIntArray
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.narcoding.localnotepad.R

/**
 * Created by Belgeler on 18.05.2017.
 */
abstract class RuntimePermissionsActivity : AppCompatActivity() {
    private var mErrorString: SparseIntArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mErrorString = SparseIntArray()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permissionCheck = PackageManager.PERMISSION_GRANTED
        for (permission in grantResults) {
            permissionCheck = permissionCheck + permission
        }
        if (grantResults.size > 0 && permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted(requestCode)
        } else {
            val builder = AlertDialog.Builder(this@RuntimePermissionsActivity)
            val message = resources.getString(R.string.runtimepermission)
            builder.setTitle(resources.getString(R.string.runtimepermission))
            builder.setMessage(message)
                    .setPositiveButton(resources.getString(R.string.okey)
                    ) { d, id ->
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.data = Uri.parse("package:$packageName")
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        startActivity(intent)
                        d.dismiss()
                    }
                    .setNegativeButton(resources.getString(R.string.cancel)
                    ) { d, id -> d.cancel() }
            builder.create().show()
        }
    }

    fun requestAppPermissions(requestedPermissions: Array<String?>,
                              stringId: Int, requestCode: Int) {
        mErrorString!!.put(requestCode, stringId)
        var permissionCheck = PackageManager.PERMISSION_GRANTED
        var shouldShowRequestPermissionRationale = false
        for (permission in requestedPermissions) {
            permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission!!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale || shouldShowRequestPermissionRationale(permission)
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale) {
                val builder = AlertDialog.Builder(this@RuntimePermissionsActivity)
                val message = resources.getString(R.string.runtimepermission)
                builder.setTitle(resources.getString(R.string.runtimepermission))
                builder.setMessage(message)
                        .setPositiveButton(resources.getString(R.string.okey)
                        ) { d, id ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(requestedPermissions, requestCode)
                            }
                            d.dismiss()
                        }
                        .setNegativeButton(resources.getString(R.string.cancel)
                        ) { d, id -> d.cancel() }
                builder.create().show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(requestedPermissions, requestCode)
                }
            }
        } else {
            onPermissionsGranted(requestCode)
        }
    }

    abstract fun onPermissionsGranted(requestCode: Int)
}