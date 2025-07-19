package com.pequesystems.gestionganadera.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.pdfview.PDFView;
import com.pequesystems.gestionganadera.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PdfActivity extends AppCompatActivity {

    PDFView vistaPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        vistaPdf = findViewById(R.id.pdf_pdfView);

        File pdfFile = copiarPdfDesdeAssets("manual_de_usuario.pdf");
        if (pdfFile != null) {
            vistaPdf.fromFile(pdfFile).show();
        } else {
            Toast.makeText(this, "Error al cargar el PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private File copiarPdfDesdeAssets(String nombreArchivo) {
        File archivoDestino = new File(getCacheDir(), nombreArchivo);
        try (InputStream input = getAssets().open(nombreArchivo);
             FileOutputStream output = new FileOutputStream(archivoDestino)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            return archivoDestino;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}