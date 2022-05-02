package me.gfuil.bmap.lite.algorithm;


import com.amap.api.maps.model.LatLng;

public class TrajectoryLCSS {
    private LatLng[] L1;
    private LatLng[] L2;
    private LatLng[] LCS;
    private double distThre;
    private double matchRatio;
    private static final double DEFAULT_DISTTHRE=0.0005;//经纬度差值阈值大约0.001在地图上相差80-90米
    private int commonLen;

    public TrajectoryLCSS(LatLng[] L1,LatLng[] L2) {
        this.L1=L1;
        this.L2=L2;
        this.distThre=DEFAULT_DISTTHRE;
    }
    /**
     * @param L1
     * @param L2
     * @param dist_thre
     */
    public TrajectoryLCSS(LatLng[] L1,LatLng[] L2,double dist_thre) {
        this(L1, L2);
        this.distThre=dist_thre;
    }
    /**
     * 动态规划计算所有子问题
     * @return
     */
    public int[][] getTypeMatrix(){
        int[][] type=new int[L1.length+1][L2.length+1];
        for(int i=L1.length-1;i>=0;i--) {
            for(int j=L2.length-1;j>=0;j--) {
                if(isClose(L1[i],L2[j])){
                    System.out.println(L1[i]);
                    System.out.println(L2[j]);
                    type[i][j]=type[i+1][j+1]+1;
                    commonLen++;
                }else {
                    type[i][j]=Math.max(type[i][j+1], type[i+1][j]);
                }
            }
        }
        return type;
    }
    /**
     * 查看两点是否可以判定为同点
     * @param p1
     * @param p2
     * @return
     */
    public boolean isClose(LatLng p1,LatLng p2) {
        double x_abs=Math.abs(p1.longitude-p2.longitude);
        double y_abs=Math.abs(p1.latitude-p2.latitude);
        if(x_abs<distThre&&y_abs<distThre)
            return true;
        return false;
    }
    /**
     * @return
     */
    public LatLng[] genLCSS() {
        int[][] typematrix=getTypeMatrix();
        LatLng[] res = new LatLng[commonLen];
        int i=0,j=0;
        int index=0;
        while(i<L1.length&&j<L2.length) {
            if(isClose(L1[i],L2[j])) {
                System.out.println(index);
                System.out.println(i);
                System.out.println(commonLen);
                System.out.println(L1[i]);
                System.out.println(L2[j]);
                res[index++]=L1[i];
                i++;
                j++;
            }else if(typematrix[i+1][j]>=typematrix[i][j+1]) {
                i++;
            }else {
                j++;
            }
        }
        LCS=res;
        matchRatio=this.LCS.length/(double)(Math.min(L1.length,L2.length));
        return res;
    }
    /**
     * 更新Ratio
     * @return
     */
    public double getMatchRatio() {
        if(matchRatio==0) {
            genLCSS();
        }
        return this.LCS.length/(double)(Math.min(L1.length,L2.length));
    }

}
