package com.squareup.spoon.html;

import com.squareup.spoon.DeviceDetails;
import com.squareup.spoon.DeviceResult;
import com.squareup.spoon.DeviceTest;
import com.squareup.spoon.DeviceTestResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.squareup.spoon.DeviceTestResult.Status;
import static java.util.stream.Collectors.toList;

/** Model for representing a {@code device.html} page. */
final class HtmlDevice {
  static HtmlDevice from(String serial, DeviceResult result, File output) {
    List<TestResult> testResults = new ArrayList<>();
    int testsPassed = 0;
    for (Map.Entry<DeviceTest, DeviceTestResult> entry : result.getTestResults().entrySet()) {
      DeviceTestResult testResult = entry.getValue();
      testResults.add(TestResult.from(serial, entry.getKey(), testResult, output));
      if (testResult.getStatus() == Status.PASS) {
        testsPassed += 1;
      }
    }

    int testsRun = result.getTestResults().size();
    int testsFailed = testsRun - testsPassed;
    String totalTestsRun = testsRun + " test" + (testsRun != 1 ? "s" : "");
    DeviceDetails details = result.getDeviceDetails();
    String title = (details != null) ? details.getName() : serial;

    List<HtmlUtils.ExceptionInfo> exceptions = result.getExceptions()
        .stream()
        .map(HtmlUtils::processStackTrace)
        .collect(toList());

//    StringBuilder subtitle1 = new StringBuilder();
//    subtitle1.append(totalTestsRun).append(" run");
//    if (testsPassed > 0) {
//      subtitle1.append(" with ")
//          .append(testsPassed)
//          .append(" passing and ")
//          .append(testsFailed)
//          .append(" failing in ")
//          .append(HtmlUtils.humanReadableDuration(result.getDuration()));
//    }
//    subtitle1.append(" at ")
//        .append(HtmlUtils.dateToString(result.getStarted()));
//
//    String subtitle2 = HtmlUtils.deviceDetailsToString(details);
//    StringBuilder subtitle1 = new StringBuilder();



    /**
     * modified by weiyaqi
     */
    StringBuilder subtitle1 = new StringBuilder();
    StringBuilder subtitle2 = new StringBuilder();
    StringBuilder subtitle3 = new StringBuilder();
    StringBuilder subtitle4 = new StringBuilder();
    StringBuilder subtitle5 = new StringBuilder();
    if (testsPassed>0) {
      subtitle1.append("Tests  Count: "+totalTestsRun+"  PASS: "+testsPassed+"    FAIL: ");
      subtitle2.append(testsFailed); //失败信息
      subtitle3.append("Spend Time: "+HtmlUtils.humanReadableDuration(result.getDuration())+"  ");
    }

    subtitle4.append("Start Time: "+HtmlUtils.dateToString(result.getStarted()));
    subtitle5.append(HtmlUtils.deviceDetailsToString(details));
    return new HtmlDevice(serial, title, subtitle1.toString(), subtitle2.toString(),
            subtitle3.toString(), subtitle4.toString(), subtitle5.toString(), testResults, exceptions);
  }

  public final String serial;
  public final String title;
  public final String subtitle1;
  public final String subtitle2;
  public final String subtitle3;
  public final String subtitle4;
  public final String subtitle5;
  public final List<TestResult> testResults;
  public final boolean hasExceptions;
  public final List<HtmlUtils.ExceptionInfo> exceptions;

  HtmlDevice(String serial, String title, String subtitle1, String subtitle2, String subtitle3,String subtitle4, String subtitle5,
      List<TestResult> testResults, List<HtmlUtils.ExceptionInfo> exceptions) {
    this.serial = serial;
    this.title = title;
    this.subtitle1 = subtitle1;
    this.subtitle2 = subtitle2;
    this.subtitle3 = subtitle3;
    this.subtitle4 = subtitle4;
    this.subtitle5 = subtitle5;

    this.testResults = testResults;
    this.hasExceptions = !exceptions.isEmpty();
    this.exceptions = exceptions;
  }

  static final class TestResult implements Comparable<TestResult> {
    static TestResult from(String serial, DeviceTest test, DeviceTestResult result, File output) {
      String className = test.getClassName();
      String methodName = test.getMethodName();
      String classSimpleName = HtmlUtils.getClassSimpleName(className);
      String prettyMethodName = HtmlUtils.prettifyMethodName(methodName);
      String testId = HtmlUtils.testClassAndMethodToId(className, methodName);
      String status = HtmlUtils.getStatusCssClass(result);
      List<HtmlUtils.Screenshot> screenshots = result.getScreenshots()
          .stream()
          .map(screenshot -> HtmlUtils.getScreenshot(screenshot, output))
          .collect(toList());
      List<HtmlUtils.SavedFile> files = result.getFiles()
          .stream()
          .map(file -> HtmlUtils.getFile(file, output))
          .collect(toList());
      String animatedGif = HtmlUtils.createRelativeUri(result.getAnimatedGif(), output);
      HtmlUtils.ExceptionInfo exception = HtmlUtils.processStackTrace(result.getException());
      return new TestResult(serial, className, methodName, classSimpleName, prettyMethodName,
          testId, status, screenshots, animatedGif, exception, files);
    }

    public final String serial;
    public final String className;
    public final String methodName;
    public final String classSimpleName;
    public final String prettyMethodName;
    public final String testId;
    public final String status;
    public final boolean hasScreenshots;
    public final List<HtmlUtils.Screenshot> screenshots;
    public final List<HtmlUtils.SavedFile> files;
    public final boolean hasFiles;
    public final String animatedGif;
    public final HtmlUtils.ExceptionInfo exception;

    TestResult(String serial, String className, String methodName, String classSimpleName,
        String prettyMethodName, String testId, String status,
        List<HtmlUtils.Screenshot> screenshots, String animatedGif,
        HtmlUtils.ExceptionInfo exception, List<HtmlUtils.SavedFile> files) {
      this.serial = serial;
      this.className = className;
      this.methodName = methodName;
      this.classSimpleName = classSimpleName;
      this.prettyMethodName = prettyMethodName;
      this.testId = testId;
      this.status = status;
      this.hasScreenshots = !screenshots.isEmpty();
      this.screenshots = screenshots;
      this.animatedGif = animatedGif;
      this.exception = exception;
      this.files = files;
      this.hasFiles = !files.isEmpty();
    }

    @Override public int compareTo(TestResult other) {
      int classComparison = className.compareTo(other.className);
      if (classComparison != 0) {
        return classComparison;
      }
      return methodName.compareTo(other.methodName);
    }
  }
}
