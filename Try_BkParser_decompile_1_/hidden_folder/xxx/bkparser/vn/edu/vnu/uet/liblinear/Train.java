package vn.edu.vnu.uet.liblinear;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Train {
   private double bias = 1.0D;
   private boolean cross_validation = false;
   private String inputFilename;
   private String modelFilename;
   private int nr_fold;
   private Parameter param = null;
   private Problem prob = null;

   public Train() {
   }

   public static void main(String[] args) throws IOException, InvalidInputDataException {
      (new Train()).run(args);
   }

   private void do_cross_validation() {
      double total_error = 0.0D;
      double sumv = 0.0D;
      double sumy = 0.0D;
      double sumvv = 0.0D;
      double sumyy = 0.0D;
      double sumvy = 0.0D;
      double[] target = new double[this.prob.l];
      long start = System.currentTimeMillis();
      Linear.crossValidation(this.prob, this.param, this.nr_fold, target);
      long stop = System.currentTimeMillis();
      System.out.println("time: " + (stop - start) + " ms");
      int total_correct;
      if (this.param.solverType.isSupportVectorRegression()) {
         for(total_correct = 0; total_correct < this.prob.l; ++total_correct) {
            double y = this.prob.y[total_correct];
            double v = target[total_correct];
            total_error += (v - y) * (v - y);
            sumv += v;
            sumy += y;
            sumvv += v * v;
            sumyy += y * y;
            sumvy += v * y;
         }

         System.out.printf("Cross Validation Mean squared error = %g%n", total_error / (double)this.prob.l);
         System.out.printf("Cross Validation Squared correlation coefficient = %g%n", ((double)this.prob.l * sumvy - sumv * sumy) * ((double)this.prob.l * sumvy - sumv * sumy) / (((double)this.prob.l * sumvv - sumv * sumv) * ((double)this.prob.l * sumyy - sumy * sumy)));
      } else {
         total_correct = 0;

         for(int i = 0; i < this.prob.l; ++i) {
            if (target[i] == this.prob.y[i]) {
               ++total_correct;
            }
         }

         System.out.printf("correct: %d%n", total_correct);
         System.out.printf("Cross Validation Accuracy = %g%%%n", 100.0D * (double)total_correct / (double)this.prob.l);
      }

   }

   private void exit_with_help() {
      System.out.printf("Usage: train [options] training_set_file [model_file]%noptions:%n-s type : set type of solver (default 1)%n  for multi-class classification%n    0 -- L2-regularized logistic regression (primal)%n    1 -- L2-regularized L2-loss support vector classification (dual)%n    2 -- L2-regularized L2-loss support vector classification (primal)%n    3 -- L2-regularized L1-loss support vector classification (dual)%n    4 -- support vector classification by Crammer and Singer%n    5 -- L1-regularized L2-loss support vector classification%n    6 -- L1-regularized logistic regression%n    7 -- L2-regularized logistic regression (dual)%n  for regression%n   11 -- L2-regularized L2-loss support vector regression (primal)%n   12 -- L2-regularized L2-loss support vector regression (dual)%n   13 -- L2-regularized L1-loss support vector regression (dual)%n-c cost : set the parameter C (default 1)%n-p epsilon : set the epsilon in loss function of SVR (default 0.1)%n-e epsilon : set tolerance of termination criterion%n   -s 0 and 2%n       |f'(w)|_2 <= eps*min(pos,neg)/l*|f'(w0)|_2,%n       where f is the primal function and pos/neg are # of%n       positive/negative data (default 0.01)%n   -s 11%n       |f'(w)|_2 <= eps*|f'(w0)|_2 (default 0.001)%n   -s 1, 3, 4 and 7%n       Dual maximal violation <= eps; similar to libsvm (default 0.1)%n   -s 5 and 6%n       |f'(w)|_1 <= eps*min(pos,neg)/l*|f'(w0)|_1,%n       where f is the primal function (default 0.01)%n   -s 12 and 13\n       |f'(alpha)|_1 <= eps |f'(alpha0)|,\n       where f is the dual function (default 0.1)\n-B bias : if bias >= 0, instance x becomes [x; bias]; if < 0, no bias term added (default -1)%n-wi weight: weights adjust the parameter C of different classes (see README for details)%n-v n: n-fold cross validation mode%n-q : quiet mode (no outputs)%n");
      System.exit(1);
   }

   Problem getProblem() {
      return this.prob;
   }

   double getBias() {
      return this.bias;
   }

   Parameter getParameter() {
      return this.param;
   }

   void parse_command_line(String[] argv) {
      this.param = new Parameter(SolverType.L2R_L2LOSS_SVC_DUAL, 1.0D, 1.0D / 0.0, 0.1D);
      this.bias = -1.0D;
      this.cross_validation = false;

      int i;
      int p;
      for(i = 0; i < argv.length && argv[i].charAt(0) == '-'; ++i) {
         ++i;
         if (i >= argv.length) {
            this.exit_with_help();
         }

         switch(argv[i - 1].charAt(1)) {
         case 'B':
            this.bias = Linear.atof(argv[i]);
            break;
         case 'c':
            this.param.setC(Linear.atof(argv[i]));
            break;
         case 'e':
            this.param.setEps(Linear.atof(argv[i]));
            break;
         case 'p':
            this.param.setP(Linear.atof(argv[i]));
            break;
         case 'q':
            --i;
            Linear.disableDebugOutput();
            break;
         case 's':
            this.param.solverType = SolverType.getById(Linear.atoi(argv[i]));
            break;
         case 'v':
            this.cross_validation = true;
            this.nr_fold = Linear.atoi(argv[i]);
            if (this.nr_fold < 2) {
               System.err.println("n-fold cross validation: n must >= 2");
               this.exit_with_help();
            }
            break;
         case 'w':
            p = Linear.atoi(argv[i - 1].substring(2));
            double weight = Linear.atof(argv[i]);
            this.param.weightLabel = addToArray(this.param.weightLabel, p);
            this.param.weight = addToArray(this.param.weight, weight);
            break;
         default:
            System.err.println("unknown option");
            this.exit_with_help();
         }
      }

      if (i >= argv.length) {
         this.exit_with_help();
      }

      this.inputFilename = argv[i];
      if (i < argv.length - 1) {
         this.modelFilename = argv[i + 1];
      } else {
         p = argv[i].lastIndexOf(47);
         ++p;
         this.modelFilename = argv[i].substring(p) + ".model";
      }

      if (this.param.eps == 1.0D / 0.0) {
         switch(this.param.solverType) {
         case L2R_LR:
         case L2R_L2LOSS_SVC:
            this.param.setEps(0.01D);
            break;
         case L2R_L2LOSS_SVR:
            this.param.setEps(0.001D);
            break;
         case L2R_L2LOSS_SVC_DUAL:
         case L2R_L1LOSS_SVC_DUAL:
         case MCSVM_CS:
         case L2R_LR_DUAL:
            this.param.setEps(0.1D);
            break;
         case L1R_L2LOSS_SVC:
         case L1R_LR:
            this.param.setEps(0.01D);
            break;
         case L2R_L1LOSS_SVR_DUAL:
         case L2R_L2LOSS_SVR_DUAL:
            this.param.setEps(0.1D);
            break;
         default:
            throw new IllegalStateException("unknown solver type: " + this.param.solverType);
         }
      }

   }

   public static Problem readProblem(File file, double bias) throws IOException, InvalidInputDataException {
      BufferedReader fp = new BufferedReader(new FileReader(file));
      List<Double> vy = new ArrayList();
      List<Feature[]> vx = new ArrayList();
      int max_index = 0;
      int lineNr = 0;

      try {
         while(true) {
            String line = fp.readLine();
            if (line == null) {
               Problem var29 = constructProblem(vy, vx, max_index, bias);
               return var29;
            }

            ++lineNr;
            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            String token;
            try {
               token = st.nextToken();
            } catch (NoSuchElementException var27) {
               throw new InvalidInputDataException("empty line", file, lineNr, var27);
            }

            try {
               vy.add(Linear.atof(token));
            } catch (NumberFormatException var26) {
               throw new InvalidInputDataException("invalid label: " + token, file, lineNr, var26);
            }

            int m = st.countTokens() / 2;
            Feature[] x;
            if (bias >= 0.0D) {
               x = new Feature[m + 1];
            } else {
               x = new Feature[m];
            }

            int indexBefore = 0;

            for(int j = 0; j < m; ++j) {
               token = st.nextToken();

               int index;
               try {
                  index = Linear.atoi(token);
               } catch (NumberFormatException var24) {
                  throw new InvalidInputDataException("invalid index: " + token, file, lineNr, var24);
               }

               if (index < 0) {
                  throw new InvalidInputDataException("invalid index: " + index, file, lineNr);
               }

               if (index <= indexBefore) {
                  throw new InvalidInputDataException("indices must be sorted in ascending order", file, lineNr);
               }

               indexBefore = index;
               token = st.nextToken();

               try {
                  double value = Linear.atof(token);
                  x[j] = new FeatureNode(index, value);
               } catch (NumberFormatException var25) {
                  throw new InvalidInputDataException("invalid value: " + token, file, lineNr);
               }
            }

            if (m > 0) {
               max_index = Math.max(max_index, x[m - 1].getIndex());
            }

            vx.add(x);
         }
      } finally {
         fp.close();
      }
   }

   void readProblem(String filename) throws IOException, InvalidInputDataException {
      this.prob = readProblem(new File(filename), this.bias);
   }

   private static int[] addToArray(int[] array, int newElement) {
      int length = array != null ? array.length : 0;
      int[] newArray = new int[length + 1];
      if (array != null && length > 0) {
         System.arraycopy(array, 0, newArray, 0, length);
      }

      newArray[length] = newElement;
      return newArray;
   }

   private static double[] addToArray(double[] array, double newElement) {
      int length = array != null ? array.length : 0;
      double[] newArray = new double[length + 1];
      if (array != null && length > 0) {
         System.arraycopy(array, 0, newArray, 0, length);
      }

      newArray[length] = newElement;
      return newArray;
   }

   private static Problem constructProblem(List<Double> vy, List<Feature[]> vx, int max_index, double bias) {
      Problem prob = new Problem();
      prob.bias = bias;
      prob.l = vy.size();
      prob.n = max_index;
      if (bias >= 0.0D) {
         ++prob.n;
      }

      prob.x = new Feature[prob.l][];

      int i;
      for(i = 0; i < prob.l; ++i) {
         prob.x[i] = (Feature[])vx.get(i);
         if (bias >= 0.0D) {
            assert prob.x[i][prob.x[i].length - 1] == null;

            prob.x[i][prob.x[i].length - 1] = new FeatureNode(max_index + 1, bias);
         }
      }

      prob.y = new double[prob.l];

      for(i = 0; i < prob.l; ++i) {
         prob.y[i] = (Double)vy.get(i);
      }

      return prob;
   }

   private void run(String[] args) throws IOException, InvalidInputDataException {
      this.parse_command_line(args);
      this.readProblem(this.inputFilename);
      if (this.cross_validation) {
         this.do_cross_validation();
      } else {
         Model model = Linear.train(this.prob, this.param);
         Linear.saveModel(new File(this.modelFilename), model);
      }

   }
}