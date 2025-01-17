package org.maltparser.parser.algorithm.planar;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.history.GuideUserHistory;

public class NivrePlanarArcEagerFactory extends PlanarFactory {
   public NivrePlanarArcEagerFactory(DependencyParserConfig _manager) {
      super(_manager);
   }

   public TransitionSystem makeTransitionSystem() throws MaltChainedException {
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Transition system    : Planar Arc-Eager\n");
      }

      return new Planar(this.manager.getPropagationManager());
   }

   public OracleGuide makeOracleGuide(GuideUserHistory history) throws MaltChainedException {
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Oracle               : Planar Arc-Eager\n");
      }

      return new PlanarArcEagerOracle(this.manager, history);
   }
}
