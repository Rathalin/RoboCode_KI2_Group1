package at.fhooe.ai.im.g1;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class bruv extends AdvancedRobot {

    static LinkedHashMap<String, Double> enemyHashMapAngles;
    static LinkedHashMap<String, Double> enemyHashMapDistances;
    static LinkedHashMap<String, Enemy> enemyHashMap;
    static double scanDir;
    static Object sought;

    private String currentTarget;

    @Override
    public void run() {

        scanDir = 1;
        currentTarget = "";
        enemyHashMapAngles = new LinkedHashMap<String, Double>(5, 2, true);
        enemyHashMapDistances = new LinkedHashMap<String, Double>(5, 2, true);
        enemyHashMap = new LinkedHashMap<String, Enemy>(5, 2, true);

        setAdjustGunForRobotTurn(true);    // Independent rotation of gun from body
        setAdjustRadarForGunTurn(true); // Independent rotation of radar from gun

        int currentTurn = 4;

        while (true) {
            setTurnRadarRightRadians(scanDir * Double.POSITIVE_INFINITY); // Keep rotating radar
            scan();
        }
    }

    private void smartTarget() {
        if (currentTarget == null) {
            return;
        }

        double e = enemyHashMapAngles.get(currentTarget);
        Enemy enemy = enemyHashMap.get(currentTarget);
        double bearingFromGun = normalRelativeAngleDegrees(e - getGunHeadingRadians());
        setTurnGunLeftRadians(bearingFromGun);

        if (getGunHeat() == 0) {
            double energy = enemy.energy;
            double dist = enemy.dist;
            double selfEnergy = getEnergy();
            double minPower = 0.3;
            double firePower = 3.0;
            if (dist > 50.0) {
                firePower -= 2.0;
            } else if (dist > 20.0) {
                firePower--;
            }

            if (selfEnergy < 20.0) {
                firePower -= 0.3;
            }

            firePower = clamp(firePower, minPower, energy);
            fire(firePower);
        }
    }

    private double clamp(double val, double min, double max) {
        if (val > max) {
            val = max;
        }
        if (val < min) {
            val = min;
        }

        return val;
    }

    public void getNearestEnemy() {
        double minDistance = Double.POSITIVE_INFINITY;
        String targetName = null;
        Iterator<String> it = enemyHashMapDistances.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            double dist = enemyHashMapDistances.get(key);
            if (dist < minDistance) {
                minDistance = dist;
                targetName = key;
            }
        }

        currentTarget = targetName;
    }

    public void onRobotDeath(RobotDeathEvent e) {
        String name = e.getName();

        // Forget robot on death
        enemyHashMap.remove(name);
        enemyHashMapAngles.remove(name);
        enemyHashMapDistances.remove(name);

        sought = null;
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        String name = e.getName();
        Enemy en;

        if (enemyHashMap.containsKey(name)) {
            en = enemyHashMap.get(name);
        } else {
            en = new Enemy();
        }

        double heading = getHeadingRadians();
        double eBearing = e.getBearingRadians();
        en.name = name;
        en.bearing = heading + eBearing;
        en.dist = e.getDistance();
        en.energy = e.getEnergy();

        double absoluteBearing = en.bearing;
        double enemyX = getX() + en.dist * Math.sin(absoluteBearing);
        double enemyY = getY() + en.dist * Math.cos(absoluteBearing);
        en.pos = new Point2D.Double(enemyX, enemyY);
        enemyHashMap.put(name, en);

        enemyHashMapAngles.put(name, absoluteBearing);
        enemyHashMapDistances.put(name, en.dist);

        getNearestEnemy();
        smartTarget();
        // Change direction of scanner
        if ((name == sought || sought == null) && enemyHashMap.size() == getOthers()) {
            scanDir = Utils.normalRelativeAngle(enemyHashMap.values().iterator().next().bearing - getRadarHeadingRadians());
            sought = enemyHashMap.keySet().iterator().next();
        }
    }
}
