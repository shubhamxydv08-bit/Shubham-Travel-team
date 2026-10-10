package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraHelper {

    fun createWatermarkedPhoto(
        context: Context,
        baseBitmap: Bitmap?,
        dutyType: String,
        entityName: String,
        locationText: String,
        coordinatesText: String,
        odometerReading: String? = null
    ): String {
        val width = 1080
        val height = 1080

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (baseBitmap != null) {
            // Scale and draw input bitmap
            val srcRect = Rect(0, 0, baseBitmap.width, baseBitmap.height)
            val destRect = Rect(0, 0, width, height)
            canvas.drawBitmap(baseBitmap, srcRect, destRect, null)
        } else {
            // Draw clean simulated vehicle camera viewfinder background
            val bgPaint = Paint().apply {
                color = Color.parseColor("#1E293B")
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Draw camera grid lines
            val gridPaint = Paint().apply {
                color = Color.parseColor("#334155")
                strokeWidth = 2f
            }
            canvas.drawLine(width / 3f, 0f, width / 3f, height.toFloat(), gridPaint)
            canvas.drawLine(width * 2 / 3f, 0f, width * 2 / 3f, height.toFloat(), gridPaint)
            canvas.drawLine(0f, height / 3f, width.toFloat(), height / 3f, gridPaint)
            canvas.drawLine(0f, height * 2 / 3f, width.toFloat(), height * 2 / 3f, gridPaint)

            // Draw center reticle / odometer frame
            val framePaint = Paint().apply {
                color = Color.parseColor("#F59E0B")
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRoundRect(width * 0.15f, height * 0.25f, width * 0.85f, height * 0.65f, 24f, 24f, framePaint)

            val labelPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("LIVE FLEET CAMERA VERIFICATION", width / 2f, height * 0.35f, labelPaint)

            val subjectPaint = Paint().apply {
                color = Color.parseColor("#FBBF24")
                textSize = 44f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(dutyType.uppercase(), width / 2f, height * 0.45f, subjectPaint)

            if (!odometerReading.isNullOrBlank()) {
                val odoPaint = Paint().apply {
                    color = Color.parseColor("#4ADE80")
                    textSize = 52f
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("ODOMETER: $odometerReading KM", width / 2f, height * 0.55f, odoPaint)
            }
        }

        // --- Draw Dark Watermark Banner at Bottom ---
        val bannerHeight = 260f
        val bannerPaint = Paint().apply {
            color = Color.argb(220, 15, 23, 42) // #0F172A with transparency
        }
        canvas.drawRect(0f, height - bannerHeight, width.toFloat(), height.toFloat(), bannerPaint)

        // Accent bar
        val accentPaint = Paint().apply {
            color = Color.parseColor("#F59E0B")
        }
        canvas.drawRect(0f, height - bannerHeight, width.toFloat(), height - bannerHeight + 8f, accentPaint)

        // Watermark Text Paint
        val headerPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val goldPaint = Paint().apply {
            color = Color.parseColor("#FBBF24")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val sdf = SimpleDateFormat("dd-MMM-yyyy | hh:mm:ss a z", Locale.US)
        val timestampStr = sdf.format(Date())

        var textY = height - bannerHeight + 48f
        canvas.drawText("SHUBHAM TRAVEL TEAM • AUDIT COMPLIANCE PROOF", 32f, textY, headerPaint)

        textY += 44f
        canvas.drawText("TAG: $dutyType • $entityName", 32f, textY, goldPaint)

        textY += 40f
        canvas.drawText("TIME: $timestampStr", 32f, textY, subPaint)

        textY += 40f
        val locText = if (locationText.length > 55) locationText.take(52) + "..." else locationText
        canvas.drawText("GPS SITE: $locText", 32f, textY, subPaint)

        textY += 40f
        canvas.drawText("COORDINATES: $coordinatesText", 32f, textY, subPaint)

        // Save to file
        val filename = "photo_${System.currentTimeMillis()}_proof.jpg"
        val storageDir = File(context.filesDir, "fleet_photos").apply { mkdirs() }
        val file = File(storageDir, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        return file.absolutePath
    }
}
