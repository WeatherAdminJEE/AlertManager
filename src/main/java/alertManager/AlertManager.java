package alertManager;

import imt.org.web.commonmodel.entities.SensorAlertEntity;
import imt.org.web.commonmodel.entities.SensorDataEntity;
import imt.org.web.commonmodel.entities.SensorEntity;
import main.Main;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class AlertManager implements Runnable{
    private SensorEntity sensor;
    private List<SensorDataEntity> lstSensorData;
    private SensorAlertEntity lastAlert;

    public AlertManager(SensorEntity s){
        sensor = s;
        Main.log.debug("Demarrage d'un thread pour le sensor "+sensor.getNameSensor());
    }

    private void initAlertManager(){
        lstSensorData = Main.crudEntityFacade.getDataBySensor(this.sensor);
        Main.log.info(""+lstSensorData.size()+" mesure récupérées pour le capteur "+sensor.getNameSensor());
        lastAlert = Main.crudEntityFacade.getLastAlertBySensor(this.sensor);
    }

    /**
     * supprime les données plus anciennes que le début/fin de la dernière alerte
     */
    private void filterData(){
        Main.log.info("filtering data");
        Timestamp lastDate = getLastDate();

        Iterator it = lstSensorData.iterator();
        List<SensorDataEntity> newArrayOfData = new ArrayList<>();
        while (it.hasNext()){
            SensorDataEntity currentData = (SensorDataEntity) it.next();
            if(currentData.getDate().after(lastDate)){
                newArrayOfData.add(currentData);
            }
        }
        this.lstSensorData = newArrayOfData;

    }

    /**
     *
     * @return la date a partir de laquelle il faut chercher les données en se basant sur la
     * dernière alerte trouvée pour le capteur.
     * Si il n'existe pas d'alert cette methode renvoie null
     *
     */
    private Timestamp getLastDate(){
            if(this.lastAlert.getEndDate()!= null)
                return this.lastAlert.getEndDate();
            else
                return this.lastAlert.getStartDate();

    }

    /**
     * look for alert in lstSensorData
     */
    private void lookForAlert(){
        double limit = sensor.getSensorAlertParam().getAlertValue();
        long timeRange = sensor.getSensorAlertParam().getAlertRange().getTime();
        List<SensorAlertEntity> lstAlert = new ArrayList<>();
        for(int i=0; i< lstSensorData.size();i++){
            SensorDataEntity currentData = lstSensorData.get(i);
            if(currentData.getMeasureValue()>limit ){
                SensorAlertEntity alert = createAlert(currentData,i+1,limit,timeRange);
                if(alert!= null){
                    lstAlert.add(alert);
                }
            }
        }
        Main.log.info("nb alerte sur capteur "+sensor.getNameSensor()+" = "+lstAlert.size());
        saveAlertIntoDatabase(lstAlert);
    }

    private void saveAlertIntoDatabase(Collection<SensorAlertEntity> lstAlert){
        for(SensorAlertEntity alert : lstAlert){
            Main.crudEntityFacade.create(alert);
        }
        Main.log.info("Fin de l'enregistrement des alertes en base pour le capteur "+sensor.getNameSensor());
    }

    private SensorAlertEntity createAlert (SensorDataEntity firstData,
                                      int indexNextData, double limit, long timeRange){
        if(timeRange == 0L){
            return new SensorAlertEntity(sensor,sensor.getSensorAlertParam(),firstData.getDate(),firstData.getDate());
        }
        SensorDataEntity lastData = firstData;
        while(indexNextData<lstSensorData.size()){
            if(lstSensorData.get(indexNextData).getMeasureValue()<=limit)
                break;
            lastData=lstSensorData.get(indexNextData);
            indexNextData++;
        }

        if(lastData.getMeasureValue()>limit && lastData.getDate().getTime() - firstData.getDate().getTime() > timeRange){
            //Cas 1 l'alerte est "finie"
            //@TODO REPLACE BY EQUALS
            if(lastData.getMeasureValue()!=lstSensorData.get(lstSensorData.size()-1).getMeasureValue()){
                return new SensorAlertEntity(sensor,sensor.getSensorAlertParam(),firstData.getDate(),lastData.getDate());
            }
            //Cas 2 l'alerte n'est pas finie
            //@Todo
        }
        //l'alerte n'est pas confirmée
        return null;
    }

    @Override
    public void run() {
        this.initAlertManager();
        if(lastAlert != null){
            this.filterData();
        }
        lookForAlert();
        Main.log.info("Fin du traitement des alertes pour le capteur "+sensor.getNameSensor());
    }
}
