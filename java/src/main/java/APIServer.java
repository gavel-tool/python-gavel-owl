  
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;
import py4j.GatewayServer;

public class APIServer {

    public OWLOntologyManager getManager() {
        return  OWLManager.createOWLOntologyManager();
    }

    public void saveOntology(OWLOntology o) throws OWLOntologyStorageException {
        o.saveOntology(new ManchesterSyntaxDocumentFormat(), System.out);
    }

    public static void main(String[] args) {
        APIServer app = new APIServer();
        // app is now the gateway.entry_point
        GatewayServer server = new GatewayServer(app);
        server.start();
    }
}
