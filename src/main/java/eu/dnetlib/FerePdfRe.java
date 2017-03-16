package eu.dnetlib;

import com.google.gson.Gson;
import eu.dnetlib.data.objectstore.rmi.ObjectStoreFile;
import eu.dnetlib.data.objectstore.rmi.ObjectStoreService;
import eu.dnetlib.data.objectstore.rmi.ObjectStoreServiceException;
import eu.dnetlib.domain.EPR;
import eu.dnetlib.enabling.is.lookup.rmi.ISLookUpException;
import eu.dnetlib.enabling.resultset.rmi.ResultSetException;
import eu.dnetlib.enabling.resultset.rmi.ResultSetService;
import eu.dnetlib.utils.EPRUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by antleb on 11/8/14.
 */
public class FerePdfRe {

    public static void main(String[] args) throws ISLookUpException, ObjectStoreServiceException, ResultSetException, NoSuchAlgorithmException, KeyManagementException, IOException {
//        String objectStoreAddress = "https://beta.services.openaire.eu:8280/is/services/objectStore";
//        String rsAddress = "https://beta.services.openaire.eu:8280/is/services/resultSet";

        ExecutorService service = Executors.newFixedThreadPool(Integer.parseInt(args[0]));

        String objectStoreAddress = "http://localhost:8888/is/services/objectStore";
        String rsAddress = "http://localhost:8888/is/services/resultSet";

        long timeout = 600000L;

        //ssl();

        ObjectStoreService storeService;
        ResultSetService rsService;

        Gson gson = new Gson();

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(ObjectStoreService.class);
        factory.setAddress(objectStoreAddress);
        storeService = (ObjectStoreService) factory.create();

//        Client client = ClientProxy.getClient(objectStoreAddress);
//        if (client != null) {
//            HTTPConduit conduit = (HTTPConduit) client.getConduit();
//            HTTPClientPolicy policy = new HTTPClientPolicy();
//            policy.setConnectionTimeout(timeout);
//            policy.setReceiveTimeout(timeout);
//            conduit.setClient(policy);
//        }

        factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(ResultSetService.class);
        factory.setAddress(rsAddress);
        rsService = (ResultSetService) factory.create();

//        client = ClientProxy.getClient(rsService    );
//        if (client != null) {
//            HTTPConduit conduit = (HTTPConduit) client.getConduit();
//            HTTPClientPolicy policy = new HTTPClientPolicy();
//            policy.setConnectionTimeout(timeout);
//            policy.setReceiveTimeout(timeout);
//            conduit.setClient(policy);
//        }

        List<String> stores = storeService.getListOfObjectStores();

        for (String store : stores) {
            W3CEndpointReference w3cEpr = storeService.deliverObjects(store, 0L, new Date().getTime());
            

            EPR epr = EPRUtils.createEPR(w3cEpr);
            String rsId = epr.getParameter("ResourceIdentifier");
            int count = rsService.getNumberOfElements(rsId);

            for (int i = 0; i < count; i += 100) {
                List<String> objects = null;
                int counter =0;

                while (objects == null && counter < 5) {
                    try {
                        objects = rsService.getResult(rsId, i, i + 100, "waiting");
                    } catch (Exception e) {
                        counter++;
                    }
                }

                if (objects == null)
                    continue;



                for (int j = 0; j < objects.size(); j++) {
                    final ObjectStoreFile md = gson.fromJson(objects.get(j), ObjectStoreFile.class);

                    if (md.getFileSizeKB() < 20000) {
                        service.submit(new Runnable() {
                            @Override
                            public void run() {
                                String filename = md.getObjectID().substring(0, md.getObjectID().lastIndexOf("::")) + ".pdf";
                                String url = md.getURI().replace("http://services.openaire.eu:8280", "http://localhost:8888");

                                System.out.println(Thread.currentThread().getName() + " - " + filename);


                                try {
                                    FileOutputStream fos = new FileOutputStream("/tmp/media/pdfs/" + filename);
                                    IOUtils.copyLarge(new URL(url).openStream(), fos);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }

        }
    }
}