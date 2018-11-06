package alertManager;

import imt.org.web.commonmodel.entities.SensorEntity;
import main.Main;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainAlertManager implements  Runnable{

    private ExecutorService threadPool;

    public MainAlertManager(){
        threadPool = Executors.newFixedThreadPool(10);
    }

    @Override
    public void run() {
        Collection<SensorEntity> lstSensor = Main.crudEntityFacade.getAllSensor();
        for(SensorEntity sensor : lstSensor){
            threadPool.execute(new AlertManager(sensor));
        }

    }
}
