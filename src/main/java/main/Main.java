package main;

import alertManager.MainAlertManager;
import crud.UniversalDao;
import crud.facade.IUniversalDao;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static final Logger log = Logger.getLogger(Main.class);
    public static final IUniversalDao crudEntityFacade = new UniversalDao();

    public static void main (String [] args){
        PropertyConfigurator.configure("log4j.properties");
        log.debug("Starting alert Manager");
        System.out.println(crudEntityFacade.getAllSensor().size());
        MainAlertManager alertManager = new MainAlertManager();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(alertManager, 0, 60L, TimeUnit.SECONDS);

    }


}
