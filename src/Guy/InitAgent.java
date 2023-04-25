package Guy;

import jade.Boot;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.util.HashMap;

/**
 *
 * Initialize agents
 */
public class InitAgent {

    public static int numAgents;
    public static String nameContainerOriginal;
    public static AgentContainer controllerAgentContainer;

    public InitAgent() {
        numAgents = 0;
        nameContainerOriginal = "";
        controllerAgentContainer = null;
    }

    public InitAgent(Agent agent, String nameAgent, String nameContainer) throws StaleProxyException {
        setAgentInContainer(agent, nameAgent, nameContainer);
    }

    private void setAgentInContainer(Agent agent, String nameAgent, String nameContainer) throws StaleProxyException {

        Runtime runtime = Runtime.instance();
        if (!nameContainer.equals(nameContainerOriginal)) {
            nameContainerOriginal = nameContainer;
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.CONTAINER_NAME, nameContainer);
            controllerAgentContainer = runtime.createAgentContainer(profile);
        }

        AgentController controller;
        System.out.println("\n\n Name of agent: " + nameAgent);
        System.out.println("\n\n Agent: " + agent);
        controller = controllerAgentContainer.acceptNewAgent(nameAgent, agent);
        controller.start();
    }

    public static void init(Agent agent, String nameAgent, String nameContainer) throws StaleProxyException {
        if (numAgents == 0) {
            Boot.main(new String[]{"-gui", "-jade_domain_df_maxresult", "500"});
        }
        numAgents++;

        new InitAgent(agent, nameAgent, nameContainer);
    }

}