package com.example.chatbot;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.w3c.dom.Text;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private TextView textoTemporario;
    private EditText editTarefa;
    private Button btnGo;
    private LinearLayout chat;
    private ScrollView scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        textoTemporario = findViewById(R.id.textoTemporario);
        editTarefa = findViewById(R.id.editTarefa);
        btnGo = findViewById(R.id.btnGo);
        chat = findViewById(R.id.chat);
        scroll = findViewById(R.id.scroll);

        // Atribuindo um Listener ao botão de enviar mensagem
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VerificaCampo()){
                    CallGemini();
                }
            }
        });
    }

    // Método para fazer a requisição da API do Gemini e gerar a resposta para o usuário
    public void CallGemini(){
        EsconderTexto();
        String tarefa = editTarefa.getText().toString();
        AdicionaQuestao(tarefa);
        LimpaCampo();
        DescerScroll();

        // Abaixo estou criando um objeto do layout da mensagem do Gemini, além do textReposta.
        // Optei por não utilizar um método para realizar esta tarefa pelo fato da mensagem
        // do Gemini funcionar de forma dinâmica: a mensagem inicia com "..." para aguardar
        // o carregamento da resposta do Gemini; depois que concluído, a mensagem é modificada
        View resposta = getLayoutInflater().inflate(R.layout.resposta, null);
        TextView textResposta = resposta.findViewById(R.id.textResposta);
        chat.addView(resposta);
        textResposta.setText("...");

        // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
        GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-1.5-flash",
// Access your API key as a Build Configuration variable (see "Set up your API key" above)
                /* apiKey */ "AIzaSyBGJoNG454BlPuTpomBgaIxtq5NTC7xcJM");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(tarefa)
                .build();

                ListenableFuture <GenerateContentResponse> response = model.generateContent(content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();

                    // Atribuindo a mensagem do Gemini ao textResposta
                    textResposta.setText(resultText);
                    DescerScroll();
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            }, this.getMainExecutor());
        }
    }

    // Método para descer o scroll da conversa até o final (usado para o recebimento e envio de novas mensagens)
    public void DescerScroll(){
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    // Método para esconder o texto explicativo após o início da conversa
    public void EsconderTexto(){
        textoTemporario.setVisibility(View.GONE);
    }

    // Método para limpar o input após o envio da mensagem
    public void LimpaCampo(){
        editTarefa.setText("");
    }

    // Método para adicionar a nova mensagem do usuário à conversa
    public void AdicionaQuestao(String tarefa){

        // Criando um objeto do layout da mensagem do usuário (questão)
        View questao = getLayoutInflater().inflate(R.layout.questao, null);

        // Criando um objeto do textQuestao
        TextView textQuestao = questao.findViewById(R.id.textQuestao);

        // Atribuindo a mensagem do usuário ao textQuestao
        textQuestao.setText(tarefa);

        // Adicionando o textQuestao ao chat
        chat.addView(questao);
    }

    // Método para verificar se o input está preenchido
    public boolean VerificaCampo(){
        if(editTarefa.length() == 0){
            Toast.makeText(getApplicationContext(),"Campo vazio!", Toast.LENGTH_LONG).show();
            return false;
        }
        else{
            return true;
        }
    }
}