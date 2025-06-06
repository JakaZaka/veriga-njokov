
import * as d3 from "d3";
import { useEffect, useRef } from "react";

export default function ClosetStats({ data }) {
  const ref = useRef();

  useEffect(() => {
    if (!data || data.length === 0) return;

    const svg = d3.select(ref.current);
    svg.selectAll("*").remove();

    const width = 400;
    const height = 400;
    const radius = Math.min(width, height) / 2;

    svg.attr("width", width).attr("height", height);

    const color = d3.scaleOrdinal(d3.schemeTableau10);

    const pie = d3.pie().value(d => d.value);
    const arc = d3.arc().innerRadius(0).outerRadius(radius - 10);

    const g = svg
      .append("g")
      .attr("transform", `translate(${width / 2},${height / 2})`);

    const arcs = pie(data);

    const tooltip = d3.select("#tooltip");

    g.selectAll("path")
      .data(arcs)
      .join("path")
      .attr("d", arc)
      .attr("fill", d => color(d.data.category))
      .on("mouseover", (event, d) => {
        tooltip
          .style("display", "block")
          .html(`<strong>${d.data.category}</strong><br/>${d.data.value}`);
      })
      .on("mousemove", event => {
        tooltip
          .style("left", `${event.offsetX + 10}px`)
          .style("top", `${event.offsetY}px`);
      })
      .on("mouseout", () => tooltip.style("display", "none"));

    // Add labels
    g.selectAll("text")
      .data(arcs)
      .join("text")
      .attr("transform", d => `translate(${arc.centroid(d)})`)
      .attr("text-anchor", "middle")
      .attr("dy", "0.35em")
      .style("font-size", "12px")
      .text(d => d.data.label);
  }, [data]);

  return (
    <div style={{ position: "relative", marginTop: "40px", textAlign: "center" }}>
      <h4>Category Distribution</h4>
      <svg ref={ref}></svg>
      <div id="tooltip" style={{
        position: "absolute",
        backgroundColor: "white",
        border: "1px solid gray",
        padding: "6px 12px",
        borderRadius: "4px",
        pointerEvents: "none",
        fontSize: "12px",
        display: "none",
        boxShadow: "0 2px 6px rgba(0,0,0,0.2)"
      }} />
    </div>
  );
}
