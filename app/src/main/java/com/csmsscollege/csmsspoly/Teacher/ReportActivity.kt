package com.csmsscollege.csmsspoly.Teacher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csmsscollege.csmsspoly.R
import com.csmsscollege.csmsspoly.Teacher.Adapter.AttendanceReportAdapter
import com.csmsscollege.csmsspoly.Teacher.Model.StudentAttendance
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream

class ReportActivity : AppCompatActivity() {

    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerDepartment: Spinner
    private lateinit var btnGenerateReport: Button
    private lateinit var btnDownloadReport: Button
    private lateinit var recyclerViewAttendance: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: AttendanceReportAdapter
    private lateinit var scrollViewReport: ScrollView
    private val studentAttendanceList = mutableListOf<StudentAttendance>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Initialize views
        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerDepartment = findViewById(R.id.spinnerDepartment)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        btnDownloadReport=findViewById(R.id.btnDownloadReport)
        recyclerViewAttendance = findViewById(R.id.recyclerViewAttendance)
        scrollViewReport = findViewById(R.id.scrollViewReport)

        // Setup Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Initialize RecyclerView
        recyclerViewAttendance.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceReportAdapter(studentAttendanceList)
        recyclerViewAttendance.adapter = adapter

        // Populate spinners
        val years = arrayOf("FY", "SY", "TY")
        val departments = arrayOf("CSE", "IT", "ENTC", "Civil", "Electrical","Mechanical")
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        spinnerDepartment.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, departments)

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("CSMSS Poly").child("Attendance")

        // Generate Report Button Click
        btnGenerateReport.setOnClickListener {
            val selectedYear = spinnerYear.selectedItem.toString()
            val selectedDepartment = spinnerDepartment.selectedItem.toString()
            fetchAttendanceData(selectedYear, selectedDepartment)
        }
        btnDownloadReport.setOnClickListener {
            if (adapter.itemCount == 0) { // Check if student list is empty
                Toast.makeText(this, "No data available to generate PDF", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (checkStoragePermission()) {
                // Hide buttons before generating PDF
                btnGenerateReport.visibility = View.GONE
                btnDownloadReport.visibility = View.GONE

                generatePDF()

                // Show buttons after PDF is generated
                btnGenerateReport.postDelayed({
                    btnGenerateReport.visibility = View.VISIBLE
                    btnDownloadReport.visibility = View.VISIBLE
                }, 500) // Small delay to ensure smooth UI update
            } else {
                requestStoragePermission()
            }
        }

    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // No need for permission in Android 10+
        } else {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )
    }


    private fun generatePDF() {
        try {
            val bitmap = getBitmapFromScrollView(scrollViewReport)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            // Ensure correct path for Android 10+ (Q and above)
            val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Attendance_Report.pdf")
            } else {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Attendance_Report.pdf")
            }

            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            // Notify system about the new file (For Android versions below Q)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = Uri.fromFile(file)
                sendBroadcast(intent)
            }

            Toast.makeText(this, "PDF saved in Downloads: ${file.absolutePath}", Toast.LENGTH_SHORT).show()

            openPDF(file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }




    private fun getBitmapFromScrollView(scrollView: ScrollView): Bitmap {
        val height = scrollView.getChildAt(0).height
        val width = scrollView.width
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        scrollView.draw(canvas)
        return bitmap
    }

    private fun openPDF(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Open PDF with"))
    }


    private fun fetchAttendanceData(year: String, department: String) {
        val attendanceRef = databaseReference.child(department).child(year)
        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val studentMap = mutableMapOf<String, StudentAttendance>()

                for (dateSnapshot in snapshot.children) {
                    for (studentSnapshot in dateSnapshot.children) {
                        val enrollmentNumber = studentSnapshot.key ?: continue
                        val studentData = studentSnapshot.getValue(StudentAttendance::class.java) ?: continue

                        if (!studentMap.containsKey(enrollmentNumber)) {
                            studentMap[enrollmentNumber] = StudentAttendance(
                                studentData.name, enrollmentNumber, 0, 0
                            )
                        }

                        val studentRecord = studentMap[enrollmentNumber]!!
                        studentRecord.totalLectures++
                        if (studentData.status == "Present") {
                            studentRecord.presentCount++
                        }
                    }
                }

                // Update RecyclerView
                studentAttendanceList.clear()
                studentAttendanceList.addAll(studentMap.values)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReportActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
