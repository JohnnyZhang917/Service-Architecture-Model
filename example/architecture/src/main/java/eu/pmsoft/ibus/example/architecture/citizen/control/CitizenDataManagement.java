package eu.pmsoft.ibus.example.architecture.citizen.control;

import eu.pmsoft.ibus.example.architecture.citizen.CitizenInfo;

public interface CitizenDataManagement {
    /**
     * Get Citizen basic information data
     * @return
     */
    public CitizenInfo getInfo();

    /**
     * Request update of citizen information. personId is immutable.
     * @param update updated information object
     */
    public void updateInformation(CitizenInfo update);
}
