package crud.facade;

import imt.org.web.commonmodel.entities.SensorAlertEntity;
import imt.org.web.commonmodel.entities.SensorDataEntity;
import imt.org.web.commonmodel.entities.SensorEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Generic CRUD facade interface
 * @param <T> Entity type
 */
public interface IUniversalDao<T> {

    /**
     * Insert object
     * @param entity Entity
     */
    void create(T entity);

    /**
     * Select object
     * @param entity Entity class
     * @param primaryKey PrK
     * @return Requested object
     */
    T findByPrimaryKey(Class<T> entity, Serializable primaryKey);

    /**
     * @param sensor le capteur
     * @return l'alerte la plus récente du capteur donné
     */
    public SensorAlertEntity getLastAlertBySensor(SensorEntity sensor);

    /**
     * @param sensor sensor
     * @return les données insérées il y a moins d'une heure par le capteur donné
     */
    public List<SensorDataEntity> getDataBySensor(SensorEntity sensor);

    /**
     * @return la liste des Sensor en base
     */
    public Collection<SensorEntity> getAllSensor();


}
