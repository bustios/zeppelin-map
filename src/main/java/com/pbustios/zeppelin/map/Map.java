package com.pbustios.zeppelin.map;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.zeppelin.helium.Application;
import org.apache.zeppelin.helium.ApplicationArgument;
import org.apache.zeppelin.helium.ApplicationException;
import org.apache.zeppelin.helium.Signal;
import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterContextRunner;
import org.apache.zeppelin.interpreter.data.TableData;
import org.apache.zeppelin.interpreter.dev.ZeppelinApplicationDevServer;
import org.apache.zeppelin.resource.ResourceInfo;
import org.apache.zeppelin.resource.ResourcePool;
import org.apache.zeppelin.resource.WellKnownResource;

public class Map extends Application {
  
  private InterpreterContext interpreterContext;

  @Override
  protected void onChange(String name, Object oldObject, Object newObject) {
    // TODO Auto-generated method stub

  }

  @Override
  public void signal(Signal signal) {
    // TODO Auto-generated method stub

  }

  @Override
  public void load() throws ApplicationException, IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void run(ApplicationArgument arg, InterpreterContext context) throws ApplicationException, IOException {
    
    this.interpreterContext = context;
    
    TableData tableData = getLastTableResult();
    
      if (tableData == null) {
        context.out.write("No table data found");
        return;
      }

      if (tableData.getColumnDef().length < 2) {
        context.out.write("Minimum 2 columns are required. Latitude and longitude");
        return;
      }

      context.out.write("<div id=\"floating-panel\">");
      context.out.write("<button onclick=\"toggleHeatmap()\">Toggle Heatmap</button>");
      context.out.write("<button onclick=\"changeGradient()\">Change gradient</button>");
      context.out.write("<button onclick=\"changeRadius()\">Change radius</button>");
      context.out.write("<button onclick=\"changeOpacity()\">Change opacity</button>");
      context.out.write("</div>");
      // create element
      context.out.write("<div id=\"map\" style=\"width: 100%; height:400px;\"></div>");
      
      // include library
      context.out.write("<script>");
      context.out.writeResource("map/map.js");

      // write data
      int numRows = tableData.length()-2;
      StringBuilder points = new StringBuilder(numRows * 40);
      points.append("function getPoints() {\n");
      points.append("return [\n");
      
      for (int i = 0; i < numRows; i++) {
        points.append("new google.maps.LatLng(" + tableData.getData(i, 0) + "," + tableData.getData(i, 1) + "),\n");
      }
      
      points.setCharAt(points.length()-2, ']');
      points.append(";\n}\n");
      points.append("</script>");

      points.append("<script async defer ");
      points.append("src=\"https://maps.googleapis.com/maps/api/js?key=");
      points.append("AIzaSyDoiNPo07x_E1bY-NNRiZargDH3_DRnDH0");
      points.append("&libraries=visualization&callback=initMap\">");
      points.append("</script>");
      context.out.write(points.toString());
  }
  
  private String getPreviousParagraphId(InterpreterContext context) {
    InterpreterContextRunner previousParagraphRunner = null;

    List<InterpreterContextRunner> runners = context.getRunners();
    
    for (int i = 0; i < runners.size(); i++) {
      if (runners.get(i).getParagraphId().equals(context.getParagraphId())) {
        if (i > 0) {
          previousParagraphRunner = runners.get(i - 1);
        }
        break;
      }
    }

    if (previousParagraphRunner == null) {
      return null;
    }

    return previousParagraphRunner.getParagraphId();
  }
  
  /**
   * Get table result from previous paragraph
   * @return
   */
  public TableData getLastTableResult() {
    String previousParagraphId = getPreviousParagraphId(interpreterContext);
    if (previousParagraphId == null) {
      return null;
    }

    ResourcePool pool = interpreterContext.getResourcePool();

    String name = WellKnownResource.resourceName(
        WellKnownResource.TABLE_DATA,
        WellKnownResource.INSTANCE_RESULT,
        interpreterContext.getNoteId(), previousParagraphId);

    Collection<ResourceInfo> res = pool.search(name);
    
    if (res.isEmpty()) {
      return null;
    }

    ResourceInfo info = res.iterator().next();
    
    TableData tableData = (TableData) interpreterContext.getResourcePool().get(
        info.location(), info.name());

    return tableData;
  }

  @Override
  public void unload() throws ApplicationException, IOException {
    // TODO Auto-generated method stub

  }

  public static void main(String [] args) throws Exception {
    // create development server
    ZeppelinApplicationDevServer dev = new ZeppelinApplicationDevServer(Map.class.getName());

    // start
    dev.server.start();
    dev.server.join();
  }
}
