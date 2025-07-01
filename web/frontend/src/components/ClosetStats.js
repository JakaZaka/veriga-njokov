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

    // Dark green and peach color palette
    const palette = [
      "#225622", // dark green
      "#ffe5b4", // peach
      "#388e3c", // secondary green
      "#ffd9a0", // lighter peach
      "#b7b97a", // olive
      "#fffaf6", // light peach background
      "#a5d6a7", // light green
      "#ffb74d"  // accent peach
    ];

    const color = d3.scaleOrdinal()
      .domain(data.map(d => d.category))
      .range(palette);

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
        .style("background", "#fffaf6")
        .style("border", "1.5px solid #225622")
        .style("padding", "8px 14px")
        .style("border-radius", "6px")
        .style("pointer-events", "none")
        .style("font-size", "13px")
        .style("color", "#225622")
        .style("display", "none")
        .style("box-shadow", "0 2px 8px rgba(34,70,34,0.13)");
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
      .style("fill", "#225622")
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
          color: "#225622",
          fontSize: "1.5em",
          fontWeight: 700,
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
            borderRadius: 2,
            background: "#ffe5b4"
          }}
        ></span>
      </h4>
      <svg ref={ref}></svg>
    </div>
  );
}