import * as d3 from "d3";
import { useEffect, useRef } from "react";

export default function ClosetStats({ data }) {
  const ref = useRef();

  useEffect(() => {
    const svg = d3.select(ref.current);
    svg.selectAll("*").remove();

    const width = 400;
    const height = 400;
    const radius = Math.min(width, height) / 2 - 30;

    svg.attr("width", width).attr("height", height);

    const g = svg.append("g")
      .attr("transform", `translate(${width / 2},${height / 2})`);

    const color = d3.scaleOrdinal()
      .domain(data.map(d => d.category))
      .range(d3.schemeSet2);

    const pie = d3.pie()
      .sort(null)
      .value(d => d.value);

    const arc = d3.arc()
      .innerRadius(60)
      .outerRadius(radius);

    // Tooltip logic
    let tooltip = d3.select("#closet-stats-tooltip");
    if (tooltip.empty()) {
      tooltip = d3.select("body")
        .append("div")
        .attr("id", "closet-stats-tooltip")
        .style("position", "fixed")
        .style("background", "white")
        .style("border", "1px solid gray")
        .style("padding", "6px 12px")
        .style("border-radius", "4px")
        .style("pointer-events", "none")
        .style("font-size", "12px")
        .style("display", "none")
        .style("box-shadow", "0 2px 6px rgba(0,0,0,0.2)");
    }

    const arcs = pie(data);

    g.selectAll("path")
      .data(arcs)
      .join("path")
      .attr("d", arc)
      .attr("fill", d => color(d.data.category))
      .on("mouseover", (event, d) => {
        tooltip
          .style("display", "block")
          .html(`<strong>${d.data.category}</strong><br/>${d.data.value}`);
        d3.select(event.currentTarget).attr("opacity", 0.7);
      })
      .on("mousemove", event => {
        tooltip
          .style("left", `${event.clientX + 12}px`)
          .style("top", `${event.clientY - 32}px`);
      })
      .on("mouseout", (event) => {
        tooltip.style("display", "none");
        d3.select(event.currentTarget).attr("opacity", 1);
      });

    // Add legend
    const legend = g.append("g")
      .attr("transform", `translate(${radius + 30}, ${-radius})`);

    legend.selectAll("rect")
      .data(data)
      .join("rect")
      .attr("x", 0)
      .attr("y", (d, i) => i * 22)
      .attr("width", 18)
      .attr("height", 18)
      .attr("fill", d => color(d.category));

    legend.selectAll("text")
      .data(data)
      .join("text")
      .attr("x", 26)
      .attr("y", (d, i) => i * 22 + 13)
      .text(d => d.category)
      .style("font-size", "14px")
      .attr("alignment-baseline", "middle");


    // Clean up tooltip on unmount
    return () => {
      tooltip.style("display", "none");
    };
  }, [data]);

  return (
    <div style={{ marginTop: "40px", textAlign: "center", position: "relative" }}>
      <h4
        style={{
          textAlign: "center",
          color: "#1976d2",
          fontSize: "1.5em",
          fontWeight: 100,
          letterSpacing: "1.2px",
          marginBottom: "18px",
          marginTop: 0,
          position: "relative",
          display: "block"
        }}
      >
        Your Closet Composition
        <span
          style={{
            display: "block",
            margin: "12px auto 0 auto",
            width: 60,
            height: 4,
            borderRadius: 2
          }}
        ></span>
      </h4>
      <svg ref={ref}></svg>
    </div>
  );
}