package evoplan.entities;

import java.util.Comparator;

public enum PartnerType {
    speaker, sponsor ;
    public static Comparator<PartnerType> getComparator() {
        return Comparator.comparing(Enum::name); // Sort by name
    }
}
