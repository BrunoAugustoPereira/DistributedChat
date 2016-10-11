import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import java.util.LinkedList;
import java.util.List;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.util.Util;

public class client extends ReceiverAdapter {
    protected JChannel ch;
    protected int count=0;

    protected void start(String name) throws Exception {
        ch=new JChannel("fast.xml").name(name); //adiciona o processo novo no canal e define a configuracao no xml
        ch.setReceiver(this);
        GMS gms=(GMS)ch.getProtocolStack().findProtocol(GMS.class);
        gms.setValue("use_delta_views", false); //se nao desabilitar nao funciona o fast.xml
        ch.connect("demo"); //conecta na cluster demo 
        loop(); // onde vamos trabalhar
        Util.close(ch);
    }

    protected void loop() {
        boolean looping=true;

        View view=ch.getView();
        Address coord=view.getMembersRaw()[0]; //o coordenador sempre é o primeiro que se conectou

        List<Address> fila=new LinkedList<>(); // linkedlist pra fila de espera do recurso
        while(looping) {
            int key=Util.keyPress("[1] solicitar recurso [2] ver fila [3] ver status do recurso [x] sair");
            switch(key) {
                case '1':
                    try {
                        ch.send(coord, "msg-" + ++count); //mandar algo pro coordenador entender q ele qr acessar e tratar lah
                    }
                    catch(Exception e) { // pode ter pq lida com transmissao
                        e.printStackTrace();
                    }
                    break;
                case '2':
                    try {
                    getFila(); //falta implementar ainda
                    }
                    catch(Exception e) { // pode ter pq lida com transmissao
                        e.printStackTrace();
                    }
                    break;
                case '3':
                    try {
                    getStatus(); // falta implementar ainda
                    }
                    catch(Exception e) { // pode ter pq lida com transmissao
                        e.printStackTrace();
                    }
                    break;
                case 'x':
                    looping=false;
                    break;
            }
        }
    }

    protected void getFila() {} //coordenador que vai fornecer
    protected void getStatus() {} //coordenador que vai fornecer

    public void viewAccepted(View view) {
        System.out.println("-- view = " + view);
    }

    public void receive(Message msg) {
        System.out.println("-- msg from " + msg.src() + ": " + msg.getObject());
    }

    public static void main(String[] args) throws Exception {
        new client().start(args[0]);
    }
}