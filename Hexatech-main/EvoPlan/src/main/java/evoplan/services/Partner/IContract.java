package evoplan.services.Partner;

import java.util.List;

public interface IContract<T> {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(T t);
    List<T> getAll();
    T getOne(int id);
    List<T> getContractsByPartnerId(int partnerId);
    List<T> getContractsByPartnershipId(int partnershipId);
    void updateStatus(int contractId, String newStatus);
}
