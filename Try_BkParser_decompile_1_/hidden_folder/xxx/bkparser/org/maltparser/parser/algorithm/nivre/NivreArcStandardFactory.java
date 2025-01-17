package org.maltparser.parser.algorithm.nivre;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.history.GuideUserHistory;

public class NivreArcStandardFactory extends NivreFactory {
   public NivreArcStandardFactory(DependencyParserConfig _manager) {
      super(_manager);
   }

   public TransitionSystem makeTransitionSystem() throws MaltChainedException {
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Transition system    : Arc-Standard\n");
      }

      return new ArcStandard(this.manager.getPropagationManager());
   }

   public OracleGuide makeOracleGuide(GuideUserHistory history) throws MaltChainedException {
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Oracle               : Arc-Standard\n");
      }

      return new ArcStandardOracle(this.manager, history);
   }
}
