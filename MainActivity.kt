package com.monzer.hares

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: SecurityLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLogs)
        val btnClearLogs = findViewById<Button>(R.id.btnClearLogs)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SecurityLogAdapter()
        recyclerView.adapter = adapter

        val database = HaresDatabase.getDatabase(applicationContext)

        btnClearLogs?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                database.securityLogDao().clearLogs()
            }
            Toast.makeText(this, "تم مسح السجل بنجاح", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                database.securityLogDao().getAllLogs().collectLatest { logs ->
                    adapter.submitList(logs)
                }
            }
        }
    }
}
