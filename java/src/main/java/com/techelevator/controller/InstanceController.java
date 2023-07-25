package com.techelevator.controller;

import com.techelevator.dao.InstanceDao;
import com.techelevator.model.Instance;
import com.techelevator.model.ProductionRun;
import com.techelevator.util.SerialGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instance")
@CrossOrigin
public class InstanceController {

    //==============================================================================
    // CLASS VARIABLE(S)
    //==============================================================================

    private final InstanceDao instanceDao;

    //==============================================================================
    // CONSTRUCTOR(S)
    //==============================================================================

    public InstanceController(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    //==============================================================================
    // HANDLER METHODS
    //==============================================================================

    /**
     * <b>Adds instance(s) to the database.</b>
     * <p>
     * Uses production run information to create and add a batch of instances to the database.  Unused serial numbers are generated on the fly.
     *
     * @param run
     * @return TODO: Should return something meaningful.  Number of records added?  List of generated serials?
     */
    @RequestMapping(method = RequestMethod.POST)
    public boolean createFromRun(@RequestBody ProductionRun run) {
        if (shouldCreateInstances(run)) {
            for (int i = 1; i <= run.getVolume(); i++) {
                Instance instance = new Instance();

                instance.setSerial(generateSerial());
                instance.setProductCode(run.getProductCode());
                instance.setProductionRun(run.getRunCode());
                instance.setSequence(i);
                instance.setClaimed(false);

                instanceDao.create(instance);
            }
        }
        return false;
    }

    /**
     * <b>Returns the instance for the given serial.</b>
     *
     * @param serial
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public Instance read(@RequestParam String serial) {
        Instance instance = instanceDao.read(serial.toUpperCase());
        if (instance != null) {
            return instance;
        } else {
            throw new InstanceNotFoundException();
        }
    }

    /**
     * Returns all instances... For debugging purposes?
     * TODO: This really should not be available to just anyone. Maybe it shouldn't be available at all?
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<Instance> readAll() {
        return instanceDao.readAll();
    }

    /**
     * <b>Flips the claimed flag on the instance to true.</b>
     * TODO: Currently no way of "locking" the serial. Not sure if that's needed yet...
     *
     * @param serial
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public boolean unlock(@PathVariable String serial) {
        return instanceDao.unlock(serial.toUpperCase());
    }

    /**
     * <b>Deletes the instance.</b>
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public boolean delete(@PathVariable String id) {
        return instanceDao.delete(id);
    }

    /**
     * <b>Deletes all instance(s) for a production run.</b>
     *
     * @param run
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public boolean delete(@RequestBody ProductionRun run) {
        return instanceDao.delete(run.getRunCode());
    }

    //==============================================================================
    // PRIVATE METHODS
    //==============================================================================

    private String generateSerial() {
        SerialGenerator sg = new SerialGenerator();
        String proposedSerial = "";

        while (!serialIsValid(proposedSerial)) {
            proposedSerial = SerialGenerator.generateSerial();
        }

        return proposedSerial;
    }

    private boolean serialIsValid(String proposedSerial) {
        if (proposedSerial.isBlank()) {
            return false;
        } else if (instanceDao.contains(proposedSerial)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean shouldCreateInstances(ProductionRun run) {
        if (!run.getStatus().equals("In Progress")) {
            return false;
        }
        return true;
    }

    //==============================================================================
    // PRIVATE EXCEPTIONS
    //==============================================================================

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No instance found for this serial.")
    private class InstanceNotFoundException extends RuntimeException {
    }
}
