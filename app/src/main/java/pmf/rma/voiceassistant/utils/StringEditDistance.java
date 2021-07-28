package pmf.rma.voiceassistant.utils;

public class StringEditDistance {
    private StringEditDistance() {

    }

    private static int min3(int x, int y, int z) {
        int min = x;
        if (y < min) {
            min = y;
        }
        if (z < min) {
            min = z;
        }
        return min;
    }

    public static int getEditDistance(String str1, String str2) {
        int[][] distanceMatrix = new int[str1.length() + 1][str2.length() + 1];
        for (int i = 0; i <= str1.length(); i++) {
            distanceMatrix[i][0] = i;
        }
        for (int i = 0; i <= str2.length(); i++) {
            distanceMatrix[0][i] = i;
        }
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                char c1 = str1.charAt(i - 1);
                char c2 = str2.charAt(j - 1);
                if (c1 == c2) {
                    distanceMatrix[i][j] = distanceMatrix[i - 1][j - 1];
                } else {
                    int distDel = distanceMatrix[i - 1][j];
                    int distAdd = distanceMatrix[i][j - 1];
                    int distSub = distanceMatrix[i - 1][j - 1];
                    distanceMatrix[i][j] = 1 + min3(distDel, distAdd, distSub);
                }
            }
        }
        return distanceMatrix[str1.length()][str2.length()];
    }
}
