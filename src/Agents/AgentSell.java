package Agents;

import Guy.SellerGui;
import Model.AgentPSS;
import Model.EntityPSS;
import PROV.DM.WasAttributedTo;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Hashtable;

/**
 *
 * @author tassio
 */
public class AgentSell extends Agent {

    private static final long serialVersionUID = 1L;

    private Hashtable<String, Integer> catalogue;
    private SellerGui myGui;

    AgentPSS ag = new AgentPSS();

    @Override
    protected void setup() {

        catalogue = new Hashtable<String, Integer>();

        myGui = new SellerGui(this);
        myGui.showGui();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("selling");
        sd.setName("JADE");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            ag.setName(getAID().getName());
            ag.setTypeAgent(sd.getType());
            System.out.println("The Agent " + ag.getIdAgent() + " of type " + ag.getTypeAgent() + " was started in system");

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());

        addBehaviour(new PurchaseOrdersServer());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        myGui.dispose();

        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }

    public void updateCatalogue(final String title, final int price) {
        addBehaviour(new OneShotBehaviour() {

            private static final long serialVersionUID = 1L;

            @Override
            public void action() {
                catalogue.put(title, price);
                System.out.println(title + " inserted into catalogue. Price = " + price);

                EntityPSS et = new EntityPSS();
                et.setPrice(price);
                et.setTitle(title);

                WasAttributedTo wat = new WasAttributedTo();
                wat.setAgent(ag);
                wat.setEntity(et);
                System.out.println("The Agent " + ag.getName() + " received the product " + et.getTitle() + " with price " + et.getPrice());
            }
        });
    }

    private class OfferRequestsServer extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer price = catalogue.get(title);
                if (price != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                    if (price < 200) {
                        int nprice = (int) (price * 1.20);
                        catalogue.replace(title, price, nprice);
                    } else {
                        int nprice = (int) (price * 0.80);
                        catalogue.replace(title, price, nprice);
                    }
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = catalogue.get(title);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title + " sold to agent " + msg.getSender().getName());
                    if (price < 200) {
                        int nprice = (int) (price * 1.20);
                        catalogue.replace(title, price, nprice);
                    } else {
                        int nprice = (int) (price * 0.80);
                        catalogue.replace(title, price, nprice);
                    }
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
