package com.hashes;

import java.util.Vector;

public final class ReedSolomon {

    private GF256 field = GF256.QR_CODE_FIELD;
    private Vector<GF256Poly> cachedGenerators = new Vector<>();
    private int crcBytesCount = 4;

    public ReedSolomon(int crcBytesCount) {
        this.crcBytesCount = crcBytesCount;
        cachedGenerators.addElement(new GF256Poly(field, new int[]{1}));
    }

    public byte[] encode(byte[] bbuff) {

        int[] ibuff = new int[bbuff.length + crcBytesCount];

        for (int i = 0; i < bbuff.length; i++)
            ibuff[i] = bbuff[i] & 0xFF;

        encode(ibuff, crcBytesCount);

        bbuff = new byte[ibuff.length];
        for (int i = 0; i < ibuff.length; i++)
            bbuff[i] = (byte) ibuff[i];

        return bbuff;
    }

    public byte[] decode(byte[] bbuff) throws ReedSolomonException {
        int[] ibuff = new int[bbuff.length];

        for (int i = 0; i < bbuff.length; i++)
            ibuff[i] = bbuff[i] & 0xFF;

        decode(ibuff, crcBytesCount);

        bbuff = new byte[ibuff.length - crcBytesCount];
        for (int i = 0; i < ibuff.length - crcBytesCount; i++)
            bbuff[i] = (byte) ibuff[i];

        return bbuff;
    }

    public void encode(int[] toEncode, int ecBytes) {
        if (ecBytes == 0)
            throw new IllegalArgumentException("No error correction bytes");
        int dataBytes = toEncode.length - ecBytes;
        if (dataBytes <= 0)
            throw new IllegalArgumentException("No data bytes provided");
        GF256Poly generator = buildGenerator(ecBytes);
        int[] infoCoefficients = new int[dataBytes];
        System.arraycopy(toEncode, 0, infoCoefficients, 0, dataBytes);
        GF256Poly info = new GF256Poly(field, infoCoefficients);
        info = info.multiplyByMonomial(ecBytes, 1);
        GF256Poly remainder = info.divide(generator)[1];
        int[] coefficients = remainder.getCoefficients();
        int numZeroCoefficients = ecBytes - coefficients.length;
        for (int i = 0; i < numZeroCoefficients; i++)
            toEncode[dataBytes + i] = 0;
        System.arraycopy(coefficients, 0, toEncode, dataBytes + numZeroCoefficients, coefficients.length);
    }

    public void decode(int[] received, int twoS) throws ReedSolomonException {
        GF256Poly poly = new GF256Poly(field, received);
        int[] syndromeCoefficients = new int[twoS];
        boolean dataMatrix = field.equals(GF256.DATA_MATRIX_FIELD);
        boolean noError = true;
        for (int i = 0; i < twoS; i++) {
            // Thanks to sanfordsquires for this fix:
            int eval = poly.evaluateAt(field.exp(dataMatrix ? i + 1 : i));
            syndromeCoefficients[syndromeCoefficients.length - 1 - i] = eval;
            if (eval != 0)
                noError = false;
        }
        if (noError)
            return;
        GF256Poly syndrome = new GF256Poly(field, syndromeCoefficients);
        GF256Poly[] sigmaOmega
                = runEuclideanAlgorithm(field.buildMonomial(twoS, 1), syndrome, twoS);
        GF256Poly sigma = sigmaOmega[0];
        GF256Poly omega = sigmaOmega[1];
        int[] errorLocations = findErrorLocations(sigma);
        int[] errorMagnitudes = findErrorMagnitudes(omega, errorLocations, dataMatrix);
        for (int i = 0; i < errorLocations.length; i++) {
            int position = received.length - 1 - field.log(errorLocations[i]);
            if (position < 0)
                throw new ReedSolomonException("Bad error location");
            received[position] = GF256.addOrSubtract(received[position], errorMagnitudes[i]);
        }
    }

    private GF256Poly[] runEuclideanAlgorithm(GF256Poly a, GF256Poly b, int R)
            throws ReedSolomonException {
        // Assume a's degree is >= b's
        if (a.getDegree() < b.getDegree()) {
            GF256Poly temp = a;
            a = b;
            b = temp;
        }

        GF256Poly rLast = a;
        GF256Poly r = b;
        GF256Poly sLast = field.getOne();
        GF256Poly s = field.getZero();
        GF256Poly tLast = field.getZero();
        GF256Poly t = field.getOne();

        // Run Euclidean algorithm until r's degree is less than R/2
        while (r.getDegree() >= R / 2) {
            GF256Poly rLastLast = rLast;
            GF256Poly sLastLast = sLast;
            GF256Poly tLastLast = tLast;
            rLast = r;
            sLast = s;
            tLast = t;

            // Divide rLastLast by rLast, with quotient in q and remainder in r
            if (rLast.isZero())
                // Oops, Euclidean algorithm already terminated?
                throw new ReedSolomonException("r_{i-1} was zero");
            r = rLastLast;
            GF256Poly q = field.getZero();
            int denominatorLeadingTerm = rLast.getCoefficient(rLast.getDegree());
            int dltInverse = field.inverse(denominatorLeadingTerm);
            while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
                int degreeDiff = r.getDegree() - rLast.getDegree();
                int scale = field.multiply(r.getCoefficient(r.getDegree()), dltInverse);
                q = q.addOrSubtract(field.buildMonomial(degreeDiff, scale));
                r = r.addOrSubtract(rLast.multiplyByMonomial(degreeDiff, scale));
            }

            s = q.multiply(sLast).addOrSubtract(sLastLast);
            t = q.multiply(tLast).addOrSubtract(tLastLast);
        }

        int sigmaTildeAtZero = t.getCoefficient(0);
        if (sigmaTildeAtZero == 0)
            throw new ReedSolomonException("sigmaTilde(0) was zero");

        int inverse = field.inverse(sigmaTildeAtZero);
        GF256Poly sigma = t.multiply(inverse);
        GF256Poly omega = r.multiply(inverse);
        return new GF256Poly[]{sigma, omega};
    }

    private int[] findErrorLocations(GF256Poly errorLocator) throws ReedSolomonException {
        // This is a direct application of Chien's search
        int numErrors = errorLocator.getDegree();
        if (numErrors == 1) // shortcut
            return new int[]{errorLocator.getCoefficient(1)};
        int[] result = new int[numErrors];
        int e = 0;
        for (int i = 1; i < 256 && e < numErrors; i++)
            if (errorLocator.evaluateAt(i) == 0) {
                result[e] = field.inverse(i);
                e++;
            }
        if (e != numErrors)
            throw new ReedSolomonException("Error locator degree does not match number of roots");
        return result;
    }

    private int[] findErrorMagnitudes(GF256Poly errorEvaluator, int[] errorLocations, boolean dataMatrix) {
        // This is directly applying Forney's Formula
        int s = errorLocations.length;
        int[] result = new int[s];
        for (int i = 0; i < s; i++) {
            int xiInverse = field.inverse(errorLocations[i]);
            int denominator = 1;
            for (int j = 0; j < s; j++)
                if (i != j)
                    denominator = field.multiply(denominator,
                            GF256.addOrSubtract(1, field.multiply(errorLocations[j], xiInverse)));
            result[i] = field.multiply(errorEvaluator.evaluateAt(xiInverse),
                    field.inverse(denominator));
            // Thanks to sanfordsquires for this fix:
            if (dataMatrix)
                result[i] = field.multiply(result[i], xiInverse);
        }
        return result;
    }

    private GF256Poly buildGenerator(int degree) {
        if (degree >= cachedGenerators.size()) {
            GF256Poly lastGenerator = (GF256Poly) cachedGenerators.elementAt(cachedGenerators.size() - 1);
            for (int d = cachedGenerators.size(); d <= degree; d++) {
                GF256Poly nextGenerator = lastGenerator.multiply(new GF256Poly(field, new int[]{1, field.exp(d - 1)}));
                cachedGenerators.addElement(nextGenerator);
                lastGenerator = nextGenerator;
            }
        }
        return (GF256Poly) cachedGenerators.elementAt(degree);
    }

    public final static class GF256 {

        public static final GF256 QR_CODE_FIELD = new GF256(0x011D); // x^8 + x^4 + x^3 + x^2 + 1
        public static final GF256 DATA_MATRIX_FIELD = new GF256(0x012D); // x^8 + x^5 + x^3 + x^2 + 1
        private final int[] expTable;
        private final int[] logTable;
        private final GF256Poly zero;
        private final GF256Poly one;

        private GF256(int primitive) {
            expTable = new int[256];
            logTable = new int[256];
            int x = 1;
            for (int i = 0; i < 256; i++) {
                expTable[i] = x;
                x <<= 1; // x = x * 2; we're assuming the generator alpha is 2
                if (x >= 0x100)
                    x ^= primitive;
            }
            for (int i = 0; i < 255; i++)
                logTable[expTable[i]] = i;
            // logTable[0] == 0 but this should never be used
            zero = new GF256Poly(this, new int[]{0});
            one = new GF256Poly(this, new int[]{1});
        }

        GF256Poly getZero() {
            return zero;
        }

        GF256Poly getOne() {
            return one;
        }

        GF256Poly buildMonomial(int degree, int coefficient) {
            if (degree < 0)
                throw new IllegalArgumentException();
            if (coefficient == 0)
                return zero;
            int[] coefficients = new int[degree + 1];
            coefficients[0] = coefficient;
            return new GF256Poly(this, coefficients);
        }

        static int addOrSubtract(int a, int b) {
            return a ^ b;
        }

        int exp(int a) {
            return expTable[a];
        }

        int log(int a) {
            if (a == 0)
                throw new IllegalArgumentException();
            return logTable[a];
        }

        int inverse(int a) {
            if (a == 0)
                throw new ArithmeticException();
            return expTable[255 - logTable[a]];
        }

        int multiply(int a, int b) {
            if (a == 0 || b == 0)
                return 0;
            if (a == 1)
                return b;
            if (b == 1)
                return a;
            return expTable[(logTable[a] + logTable[b]) % 255];
        }
    }

    public final static class GF256Poly {

        private final GF256 field;
        private final int[] coefficients;

        GF256Poly(GF256 field, int[] coefficients) {
            if (coefficients == null || coefficients.length == 0)
                throw new IllegalArgumentException();
            this.field = field;
            int coefficientsLength = coefficients.length;
            if (coefficientsLength > 1 && coefficients[0] == 0) {
                // Leading term must be non-zero for anything except the constant polynomial "0"
                int firstNonZero = 1;
                while (firstNonZero < coefficientsLength && coefficients[firstNonZero] == 0)
                    firstNonZero++;
                if (firstNonZero == coefficientsLength)
                    this.coefficients = field.getZero().coefficients;
                else {
                    this.coefficients = new int[coefficientsLength - firstNonZero];
                    System.arraycopy(coefficients,
                            firstNonZero,
                            this.coefficients,
                            0,
                            this.coefficients.length);
                }
            } else
                this.coefficients = coefficients;
        }

        int[] getCoefficients() {
            return coefficients;
        }

        int getDegree() {
            return coefficients.length - 1;
        }

        boolean isZero() {
            return coefficients[0] == 0;
        }

        int getCoefficient(int degree) {
            return coefficients[coefficients.length - 1 - degree];
        }

        int evaluateAt(int a) {
            if (a == 0)
                // Just return the x^0 coefficient
                return getCoefficient(0);
            int size = coefficients.length;
            if (a == 1) {
                // Just the sum of the coefficients
                int result = 0;
                for (int i = 0; i < size; i++)
                    result = GF256.addOrSubtract(result, coefficients[i]);
                return result;
            }
            int result = coefficients[0];
            for (int i = 1; i < size; i++)
                result = GF256.addOrSubtract(field.multiply(a, result), coefficients[i]);
            return result;
        }

        GF256Poly addOrSubtract(GF256Poly other) {
            if (!field.equals(other.field))
                throw new IllegalArgumentException("GF256Polys do not have same GF256 field");
            if (isZero())
                return other;
            if (other.isZero())
                return this;

            int[] smallerCoefficients = this.coefficients;
            int[] largerCoefficients = other.coefficients;
            if (smallerCoefficients.length > largerCoefficients.length) {
                int[] temp = smallerCoefficients;
                smallerCoefficients = largerCoefficients;
                largerCoefficients = temp;
            }
            int[] sumDiff = new int[largerCoefficients.length];
            int lengthDiff = largerCoefficients.length - smallerCoefficients.length;
            // Copy high-order terms only found in higher-degree polynomial's coefficients
            System.arraycopy(largerCoefficients, 0, sumDiff, 0, lengthDiff);

            for (int i = lengthDiff; i < largerCoefficients.length; i++)
                sumDiff[i] = GF256.addOrSubtract(smallerCoefficients[i - lengthDiff], largerCoefficients[i]);

            return new GF256Poly(field, sumDiff);
        }

        GF256Poly multiply(GF256Poly other) {
            if (!field.equals(other.field))
                throw new IllegalArgumentException("GF256Polys do not have same GF256 field");
            if (isZero() || other.isZero())
                return field.getZero();
            int[] aCoefficients = this.coefficients;
            int aLength = aCoefficients.length;
            int[] bCoefficients = other.coefficients;
            int bLength = bCoefficients.length;
            int[] product = new int[aLength + bLength - 1];
            for (int i = 0; i < aLength; i++) {
                int aCoeff = aCoefficients[i];
                for (int j = 0; j < bLength; j++)
                    product[i + j] = GF256.addOrSubtract(product[i + j],
                            field.multiply(aCoeff, bCoefficients[j]));
            }
            return new GF256Poly(field, product);
        }

        GF256Poly multiply(int scalar) {
            if (scalar == 0)
                return field.getZero();
            if (scalar == 1)
                return this;
            int size = coefficients.length;
            int[] product = new int[size];
            for (int i = 0; i < size; i++)
                product[i] = field.multiply(coefficients[i], scalar);
            return new GF256Poly(field, product);
        }

        GF256Poly multiplyByMonomial(int degree, int coefficient) {
            if (degree < 0)
                throw new IllegalArgumentException();
            if (coefficient == 0)
                return field.getZero();
            int size = coefficients.length;
            int[] product = new int[size + degree];
            for (int i = 0; i < size; i++)
                product[i] = field.multiply(coefficients[i], coefficient);
            return new GF256Poly(field, product);
        }

        GF256Poly[] divide(GF256Poly other) {
            if (!field.equals(other.field))
                throw new IllegalArgumentException("GF256Polys do not have same GF256 field");
            if (other.isZero())
                throw new IllegalArgumentException("Divide by 0");

            GF256Poly quotient = field.getZero();
            GF256Poly remainder = this;

            int denominatorLeadingTerm = other.getCoefficient(other.getDegree());
            int inverseDenominatorLeadingTerm = field.inverse(denominatorLeadingTerm);

            while (remainder.getDegree() >= other.getDegree() && !remainder.isZero()) {
                int degreeDifference = remainder.getDegree() - other.getDegree();
                int scale = field.multiply(remainder.getCoefficient(remainder.getDegree()), inverseDenominatorLeadingTerm);
                GF256Poly term = other.multiplyByMonomial(degreeDifference, scale);
                GF256Poly iterationQuotient = field.buildMonomial(degreeDifference, scale);
                quotient = quotient.addOrSubtract(iterationQuotient);
                remainder = remainder.addOrSubtract(term);
            }

            return new GF256Poly[]{quotient, remainder};
        }

        public String toString() {
            StringBuilder result = new StringBuilder(8 * getDegree());
            for (int degree = getDegree(); degree >= 0; degree--) {
                int coefficient = getCoefficient(degree);
                if (coefficient != 0) {
                    if (coefficient < 0) {
                        result.append(" - ");
                        coefficient = -coefficient;
                    } else
                        if (result.length() > 0)
                            result.append(" + ");
                    if (degree == 0 || coefficient != 1) {
                        int alphaPower = field.log(coefficient);
                        if (alphaPower == 0)
                            result.append('1');
                        else
                            if (alphaPower == 1)
                                result.append('a');
                            else {
                                result.append("a^");
                                result.append(alphaPower);
                            }
                    }
                    if (degree != 0)
                        if (degree == 1)
                            result.append('x');
                        else {
                            result.append("x^");
                            result.append(degree);
                        }
                }
            }
            return result.toString();
        }
    }

    public final static class ReedSolomonException extends Exception {

        public ReedSolomonException(String message) {
            super(message);
        }
    }
}
