package common;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import server.Server;
import spark.Spark;

//TODO put this into a testing utility in the same protobuf-http-utils library
public class Base {
    @Before
    public void init() {
        Server.main();
        Spark.awaitInitialization();
    }

    @After
    public void teardown() throws InterruptedException {
        Spark.stop();
        //this really shouldn't be necessary...
        Thread.sleep(10);
    }
}
