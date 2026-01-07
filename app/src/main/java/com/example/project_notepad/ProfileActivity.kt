package com.example.project_notepad

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val PICK_PROFILE = 100
        const val PICK_BG = 101
        const val PERMISSION_REQUEST = 200
    }

    private lateinit var imgProfile: ImageView
    private lateinit var imgBackground: ImageView
    private lateinit var prefs: SharedPreferences

    private var isPickingProfile = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnChangePhoto = findViewById<TextView>(R.id.btnChangePhoto)
        val btnChangeBg = findViewById<TextView>(R.id.btnChangeBg)

        imgProfile = findViewById(R.id.imgProfile)
        imgBackground = findViewById(R.id.imgBackground)

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        loadSavedImages()

        btnBack.setOnClickListener { finish() }

        btnChangePhoto.setOnClickListener {
            isPickingProfile = true
            openGallery()
        }

        btnChangeBg.setOnClickListener {
            isPickingProfile = false
            openGallery()
        }
    }


    // ================= GALLERY =================

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT
        ).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(
            intent,
            if (isPickingProfile) PICK_PROFILE else PICK_BG
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null) return

        val uri = data.data ?: return

        // untuk simpan izin permanen
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        if (requestCode == PICK_PROFILE) {
            imgProfile.setImageURI(uri)
            prefs.edit().putString("profile_uri", uri.toString()).apply()
        }

        if (requestCode == PICK_BG) {
            imgBackground.setImageURI(uri)
            prefs.edit().putString("bg_uri", uri.toString()).apply()
        }
    }

    // ================= LOAD SAVED =================
    private fun loadSavedImages() {
        loadImageSafely("profile_uri", imgProfile)
        loadImageSafely("bg_uri", imgBackground)
    }

    private fun loadImageSafely(key: String, imageView: ImageView) {
        val uriString = prefs.getString(key, null) ?: return

        try {
            val uri = Uri.parse(uriString)

            // test akses dulu
            contentResolver.openInputStream(uri)?.close()

            imageView.setImageURI(uri)

        } catch (e: Exception) {
            e.printStackTrace()

            // hapus uri rusak
            prefs.edit().remove(key).apply()
        }
    }

    // ================= PERMISSION RESULT =================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
