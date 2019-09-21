/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.kbest;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.kbest.Candidate;
import org.maltparser.parser.history.kbest.ScoredCandidate;

public class KBestList {
    protected final ArrayList<Candidate> kBestList;
    protected int k = -1;
    protected int topCandidateIndex;
    protected int addCandidateIndex;
    protected final SingleDecision decision;

    public KBestList(SingleDecision decision) {
        this(-1, decision);
    }

    public KBestList(Integer k, SingleDecision decision) {
        this.setK(k);
        this.decision = decision;
        if (this.k > 0) {
            this.kBestList = new ArrayList(this.k);
            this.initKBestList();
        } else {
            this.kBestList = new ArrayList();
        }
    }

    protected void initKBestList() {
        for (int i = 0; i < this.k; ++i) {
            this.kBestList.add(new Candidate());
        }
    }

    public void reset() {
        this.topCandidateIndex = 0;
        this.addCandidateIndex = 0;
    }

    public void add(int actionCode) throws MaltChainedException {
        if (this.k != -1 && this.addCandidateIndex >= this.k) {
            return;
        }
        if (this.addCandidateIndex >= this.kBestList.size()) {
            this.kBestList.add(new Candidate());
        }
        this.kBestList.get(this.addCandidateIndex).setActionCode(actionCode);
        if (this.addCandidateIndex == 0) {
            this.decision.addDecision(actionCode);
            ++this.topCandidateIndex;
        }
        ++this.addCandidateIndex;
    }

    public void addList(int[] predictionList) throws MaltChainedException {
        int n = this.k != -1 && this.k <= predictionList.length - 1 ? this.k : predictionList.length - 1;
        for (int i = 0; i < n; ++i) {
            this.add(predictionList[i]);
        }
    }

    public void add(String symbol) throws MaltChainedException {
        this.add(this.decision.getDecisionCode(symbol));
    }

    public boolean updateActionWithNextKBest() throws MaltChainedException {
        if (this.addCandidateIndex != 0 && this.topCandidateIndex < this.addCandidateIndex && this.topCandidateIndex < this.kBestList.size()) {
            int actionCode = this.kBestList.get(this.topCandidateIndex).getActionCode();
            if (this.decision instanceof SingleDecision) {
                this.decision.addDecision(actionCode);
            }
            ++this.topCandidateIndex;
            return true;
        }
        return false;
    }

    public int peekNextKBest() {
        if (this.addCandidateIndex != 0 && this.topCandidateIndex < this.addCandidateIndex && this.topCandidateIndex < this.kBestList.size()) {
            return this.kBestList.get(this.topCandidateIndex).getActionCode();
        }
        return -1;
    }

    public int getCurrentSize() {
        return this.addCandidateIndex;
    }

    public int getK() {
        return this.k;
    }

    protected void setK(int k) {
        if (k == 0) {
            this.k = 1;
        }
        this.k = k < 0 ? -1 : k;
    }

    protected int getTopCandidateIndex() {
        return this.topCandidateIndex;
    }

    protected int getAddCandidateIndex() {
        return this.addCandidateIndex;
    }

    public SingleDecision getDecision() {
        return this.decision;
    }

    public int getKBestListSize() {
        return this.kBestList.size();
    }

    public ScoredCandidate getCandidate(int i) {
        if (i >= this.kBestList.size()) {
            return null;
        }
        return (ScoredCandidate)this.kBestList.get(i);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < this.addCandidateIndex; ++i) {
            sb.append(this.kBestList.get(i));
            sb.append(' ');
        }
        sb.append("] ");
        return sb.toString();
    }
}
