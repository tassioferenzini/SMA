package Guy;

import Agents.AgentBuy;
import Agents.AgentSell;
import jade.wrapper.StaleProxyException;

/**
 *
 * @author tassio
 */
public class Main {

    /**
     * @param arg the command line arguments
     */
    public static void main(String[] arg) throws StaleProxyException, InterruptedException {

        String[] args = {"java", "php", "c", "pao"};

        AgentSell s1 = new AgentSell();
        InitAgent.init(s1, "s1", "Seller");
        Thread.currentThread().sleep(5000);
        s1.updateCatalogue("c", 50);
        s1.updateCatalogue("php", 30);
        s1.updateCatalogue("java", 80);

        AgentSell s2 = new AgentSell();
        InitAgent.init(s2, "s2", "Seller2");
        Thread.currentThread().sleep(5000);
        s2.updateCatalogue("c", 50);
        s2.updateCatalogue("php", 80);
        s2.updateCatalogue("java", 50);

        AgentBuy b1 = new AgentBuy();
        b1.setArguments(args);
        InitAgent.init(b1, "b1", "Buyer1");
        Thread.currentThread().sleep(5000);

        AgentBuy b2 = new AgentBuy();
        b2.setArguments(args);
        InitAgent.init(b2, "b2", "Buyer2");

    }

}