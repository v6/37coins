package com._37coins.util;

import java.util.Comparator;

import com._37coins.persistence.dao.Gateway;

public class GatewayPriceComparator implements Comparator<Gateway> {

    @Override
    public int compare(Gateway o1, Gateway o2) {
        return o1.getSettings().getFee().compareTo(o2.getSettings().getFee());
    }

}
