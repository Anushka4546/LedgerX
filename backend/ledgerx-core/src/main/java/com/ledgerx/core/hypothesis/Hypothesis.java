package com.ledgerx.core.hypothesis;

import java.util.Set;

public interface Hypothesis {

    String id();

    String description();

    Set<String> tags();
}
