package com.example.myapplication.utils

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning

/**
 * Single ML Kit scanner instance — avoids allocating a new client per camera frame.
 */
object BarcodeScannerProvider {
    val scanner: BarcodeScanner by lazy { BarcodeScanning.getClient() }
}
