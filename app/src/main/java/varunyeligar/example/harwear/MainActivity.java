package varunyeligar.example.harwear;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends WearableActivity {

    private TextView mTextView;


    Interpreter interpreter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();

        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final TextView result = findViewById(R.id.textView);

        Button run = findViewById(R.id.button);

        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                result.setText("Calculating...");
                String[] content = null;
                try {
                    InputStream inputStream = getAssets().open("comsnets_data.csv");
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    int count = 0;
                    String line = "";
                    long start = System.currentTimeMillis();
                    while((line = br.readLine()) != null){
                        content = line.split(",");

                        float[][][] input_array = new float [1][128][9];
                        int k =0;
                        for(int i=0;i<128;i++) {
                            for(int j=0;j<9;j++) {
                                input_array[0][i][j] = Float.parseFloat(content[k++]);
                            }
                        }

                        float[][] output = new float[1][6];
                        interpreter.run(input_array,output);

                        count++;

                        //break;
                    }

                    long end = System.currentTimeMillis();
                    result.setText(end - start +"ms");
                    //result.setText(count+"hello");
                    br.close();
                } catch (IOException e) {
                    result.setText("not found");
                    e.printStackTrace();
                }
            }
        });
    }

    private MappedByteBuffer loadModelFile() throws IOException
    {
        AssetFileDescriptor assetFileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long length = assetFileDescriptor.getLength();
        return  fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,length);
    }
}
