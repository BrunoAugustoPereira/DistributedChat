import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import java.util.ArrayList;
import java.util.List;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.util.Util;

/**
 * @author Bela Ban
 * @since x.y
 */
public class bla2 extends ReceiverAdapter {
    protected JChannel ch;
    protected int count=0;

    protected void start(String name) throws Exception {
        ch=new JChannel("fast.xml").name(name);
        ch.setReceiver(this);
        GMS gms=(GMS)ch.getProtocolStack().findProtocol(GMS.class);
        gms.setValue("use_delta_views", false);
        ch.connect("demo");
        loop();
        Util.close(ch);
    }

    protected void loop() {
        boolean looping=true;
        while(looping) {
            int key=Util.keyPress("[1] mcast message [2] change coord [3] print view [x] exit");
            switch(key) {
                case '1':
                    try {
                        ch.send(null, "msg-" + ++count);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case '2':
                    changeView();
                    break;
                case '3':
                    System.out.println("view: " + ch.getView());
                    break;
                case 'x':
                    looping=false;
                    break;
            }
        }
    }

    protected void changeView() {
        View view=ch.getView();
        Address local_addr=ch.getAddress();
        Address coord=view.getMembersRaw()[0];
        if(!local_addr.equals(coord)) {
            System.err.println("View can only be changed on coordinator");
            return;
        }
        if(view.size() == 1) {
            System.err.println("Coordinator cannot change as view only has a single member");
            return;
        }
        List<Address> mbrs=new ArrayList<>(view.getMembers());
        long new_id=view.getViewId().getId() + 1;

        Address tmp_coord=mbrs.remove(0);
        mbrs.add(tmp_coord);
        View new_view=new View(mbrs.get(0), new_id, mbrs);
        GMS gms=(GMS)ch.getProtocolStack().findProtocol(GMS.class);
        gms.castViewChange(new_view, null, mbrs);
    }

    public void viewAccepted(View view) {
        System.out.println("-- view = " + view);
    }

    public void receive(Message msg) {
        System.out.println("-- msg from " + msg.src() + ": " + msg.getObject());
    }

    public static void main(String[] args) throws Exception {
        new bla2().start(args[0]);
    }
}