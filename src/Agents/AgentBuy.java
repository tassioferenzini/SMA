/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import Model.ActivityPSS;
import Model.AgentPSS;
import PROV.DM.WasAssociatedWith;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.time.Instant;
import java.util.Date;

/**
 *
 * @author tassio
 */
public class AgentBuy extends Agent {

    private static final long serialVersionUID = 1L;

    private String targetTitle;
    private AID[] sellerAgents;
    AgentPSS ag = new AgentPSS();

    @Override
    protected void setup() {
        System.out.println("Hello! Buyer-agent " + getAID().getName() + " is ready.");

        ag.setName(getAID().getName());
        ag.setTypeAgent("buyer");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetTitle = (String) args[0];
            System.out.println("Target product is " + targetTitle);

            addBehaviour(new TickerBehaviour(this, 10000) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onTick() {
                    System.out.println("Trying to buy " + targetTitle);
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("selling");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following seller agents:");
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            sellerAgents[i] = result[i].getName();
                            System.out.println(sellerAgents[i].getName());
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }

                    myAgent.addBehaviour(new RequestPerformer());
                }
            });
        } else {
            System.out.println("No target product title specified");
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Buyer-agent " + getAID().getName() + " terminating.");
    }

    private class RequestPerformer extends Behaviour {

        private static final long serialVersionUID = 1L;

        private AID bestSeller;
        private int bestPrice;
        private int repliesCnt = 0;
        private MessageTemplate mt;
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    ActivityPSS ac = new ActivityPSS();
                    ac.setDescription("product-trade");
                    Date startTime = Date.from(Instant.now());
                    ac.setStartTime(startTime);

                    // Send the ACLMessage to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.length; ++i) {
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetTitle);
                    cfp.setConversationId("product-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("product-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;

                    Date endTime = Date.from(Instant.now());
                    ac.setEndTime(endTime);
                    System.out.println("The activity " + ac.getDescription() + " started in " + ac.getStartTime() + "and finished " + ac.getEndTime());

                    break;
                case 1:

                    ActivityPSS ac2 = new ActivityPSS();
                    ac2.setDescription("product-trade");
                    Date startTime2 = Date.from(Instant.now());
                    ac2.setStartTime(startTime2);

                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.length) {
                            step = 2;
                        }
                    } else {
                        block();
                    }

                    Date endTime2 = Date.from(Instant.now());
                    ac2.setEndTime(endTime2);
                    System.out.println("The activity " + ac2.getDescription() + " started in " + ac2.getStartTime() + "and finished " + ac2.getEndTime());

                    break;
                case 2:

                    ActivityPSS ac3 = new ActivityPSS();
                    ac3.setDescription("product-trade");
                    Date startTime3 = Date.from(Instant.now());
                    ac3.setStartTime(startTime3);

                    // Send the purchase order to the seller that provided the best offer
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetTitle);
                    order.setConversationId("product-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    mt = MessageTemplate.and(
                            MessageTemplate.MatchConversationId("product-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;

                    Date endTime3 = Date.from(Instant.now());
                    ac3.setEndTime(endTime3);
                    System.out.println("The activity " + ac3.getDescription() + " started in " + ac3.getStartTime() + "and finished " + ac3.getEndTime());

                    break;
                case 3:

                    ActivityPSS ac4 = new ActivityPSS();
                    ac4.setDescription("product-trade");
                    Date startTime4 = Date.from(Instant.now());
                    ac4.setStartTime(startTime4);

                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            System.out.println(targetTitle
                                    + " successfully purchased from agent "
                                    + reply.getSender().getName());
                            System.out.println("Price = " + bestPrice);

                            WasAssociatedWith waw = new WasAssociatedWith();
                            waw.setActivity(ac4);
                            waw.setAgent(ag);
                            waw.setPlan("Successfully purchased");
                            System.out.println("The agent" + ag.getName() + " successfully purchased " + targetTitle);
                            // myAgent.doDelete();
                        } else {
                            System.out.println("Attempt failed: requested product already sold.");
                        }
                        step = 4;
                    } else {
                        block();
                    }

                    Date endTime4 = Date.from(Instant.now());
                    ac4.setEndTime(endTime4);
                    System.out.println("The activity " + ac4.getDescription() + " started in " + ac4.getStartTime() + "and finished " + ac4.getEndTime());

                    break;
            }
        }

        @Override
        public boolean done() {
            if (step == 2 && bestSeller == null) {
                System.out.println("Attempt failed: " + targetTitle + " not available for sale");
            }
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }
}
