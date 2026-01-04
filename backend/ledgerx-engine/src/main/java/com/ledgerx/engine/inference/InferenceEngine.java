package com.ledgerx.engine.inference;

import com.ledgerx.core.belief.BeliefState;
import com.ledgerx.core.evidence.Evidence;

import java.util.List;

public interface InferenceEngine {

    BeliefState update(BeliefState current, List<Evidence> evidence);
}
