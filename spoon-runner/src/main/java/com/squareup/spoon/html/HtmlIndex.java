package com.squareup.spoon.html;

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.squareup.spoon.DeviceDetails;
import com.squareup.spoon.DeviceResult;
import com.squareup.spoon.DeviceTest;
import com.squareup.spoon.DeviceTestResult;
import com.squareup.spoon.SpoonSummary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.squareup.spoon.DeviceTestResult.Status;
import static java.util.stream.Collectors.toList;

/** Model for representing the {@code index.html} page. */
final class HtmlIndex {
  static HtmlIndex from(SpoonSummary summary) {
    int testsRun = 0;
    int totalSuccess = 0;
    List<Device> devices = new ArrayList<>();
    for (Map.Entry<String, DeviceResult> result : summary.getResults().entrySet()) {
      devices.add(Device.from(result.getKey(), result.getValue()));
      Map<DeviceTest, DeviceTestResult> testResults = result.getValue().getTestResults();
      testsRun += testResults.size();
      for (Map.Entry<DeviceTest, DeviceTestResult> entry : testResults.entrySet()) {
        if (entry.getValue().getStatus() == Status.PASS) {
          totalSuccess += 1;
        }
      }
    }

    Collections.sort(devices);
    /**
     <h2>Summary Execution</h2>
     <p>Tests Count:3 &nbsp;&nbsp;Device Count:1&nbsp;&nbsp;Pass:3&nbsp;&nbsp;<font color="red">Fail:0</font></p>
     <p>Spend Time: 7 minutes, 58 seconds &nbsp;&nbsp; &nbsp;&nbsp; Start Time: 2018-10-28 09:38 上午</p>
     */
    //9 tests run across 1 device with 7 passing and 2 failing in 6 minutes, 33 seconds at 2018-10-27 12:42 下午

    int totalFailure = testsRun - totalSuccess;

    int deviceCount = summary.getResults().size();
    IRemoteAndroidTestRunner.TestSize testSize = summary.getTestSize();
    String started = HtmlUtils.dateToString(summary.getStarted());
    String totalDevices = String.valueOf(deviceCount);
/*    String totalTestsRun = "Test count:"+ testsRun + (testSize != null ? " " + testSize.name().toLowerCase() : "") +
           " test" + (testsRun != 1 ? "s" : "");

    StringBuilder subtitle = new StringBuilder();
    subtitle.append(totalTestsRun).append(" run across ").append(totalDevices);
    if (testsRun > 0) {
      subtitle.append(" with ")
          .append(totalSuccess)
          .append(" passing and ")
          .append(totalFailure)
          .append(" failing in ")
          .append(HtmlUtils.humanReadableDuration(summary.getDuration()));
    }
    subtitle.append(" at ").append(started);
    */
    /**
     * modified by weiyaqi
     */
    StringBuilder subtitle1 = new StringBuilder();
    StringBuilder subtitle2 = new StringBuilder();
    StringBuilder subtitle3 = new StringBuilder();
    StringBuilder subtitle4 = new StringBuilder();

    if(testsRun>0) {
      subtitle1.append("Tests  Count: "+testsRun+"  Device Count: "+totalDevices
              +"  PASS: "+totalSuccess+"  FAIL: ");
      subtitle2.append(totalFailure); //失败信息
      subtitle3.append("Spend Time: "+HtmlUtils.humanReadableDuration(summary.getDuration())+"  ");
    }
    subtitle4.append("Start Time: "+started);
    return new HtmlIndex(summary.getTitle(), subtitle1.toString(), subtitle2.toString(),
            subtitle3.toString(), subtitle4.toString(),devices);
  }

  public final String title;
  public final String subtitle1;
  public final String subtitle2;
  public final String subtitle3;
  public final String subtitle4;
  public final List<Device> devices;

  HtmlIndex(String title, String subtitle1, String subtitle2, String subtitle3, String subtitle4, List<Device> devices) {
    this.title = title;
    this.subtitle1 = subtitle1;
    this.subtitle2 = subtitle2;
    this.subtitle3 = subtitle3;
    this.subtitle4 = subtitle4;
    this.devices = devices;
  }

  static final class Device implements Comparable<Device> {
    static Device from(String serial, DeviceResult result) {
      List<TestResult> testResults = result.getTestResults()
          .entrySet()
          .stream()
          .map(entry -> TestResult.from(serial, entry.getKey(), entry.getValue()))
          .collect(toList());
      DeviceDetails details = result.getDeviceDetails();
      String name = (details != null) ? details.getName() : serial;
      boolean executionFailed = testResults.isEmpty() && !result.getExceptions().isEmpty();
      return new Device(serial, name, testResults, executionFailed);
    }

    public final String serial;
    public final String name;
    public final List<TestResult> testResults;
    public final boolean executionFailed;
    public final int testCount;

    Device(String serial, String name, List<TestResult> testResults, boolean executionFailed) {
      this.serial = serial;
      this.name = name;
      this.testResults = testResults;
      this.testCount = testResults.size();
      this.executionFailed = executionFailed;
    }

    @Override public int compareTo(Device other) {
      if (name == null && other.name == null) {
        return serial.compareTo(other.serial);
      }
      if (name == null) {
        return 1;
      }
      if (other.name == null) {
        return -1;
      }
      return name.compareTo(other.name);
    }

    @Override public String toString() {
      return name != null ? name : serial;
    }
  }

  static final class TestResult implements Comparable<TestResult> {
    static TestResult from(String serial, DeviceTest test, DeviceTestResult testResult) {
      String className = test.getClassName();
      String methodName = test.getMethodName();
      String classSimpleName = HtmlUtils.getClassSimpleName(className);
      String prettyMethodName = HtmlUtils.prettifyMethodName(methodName);
      String testId = HtmlUtils.testClassAndMethodToId(className, methodName);
      String status = HtmlUtils.getStatusCssClass(testResult);
      return new TestResult(serial, classSimpleName, prettyMethodName, testId, status);
    }

    public final String serial;
    public final String classSimpleName;
    public final String prettyMethodName;
    public final String testId;
    public final String status;

    TestResult(String serial, String classSimpleName, String prettyMethodName, String testId,
        String status) {
      this.serial = serial;
      this.classSimpleName = classSimpleName;
      this.prettyMethodName = prettyMethodName;
      this.testId = testId;
      this.status = status;
    }

    @Override public int compareTo(TestResult other) {
      return 0;
    }
  }
}
