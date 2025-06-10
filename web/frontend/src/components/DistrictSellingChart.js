import * as d3 from "d3";
import { useEffect, useRef } from "react";

export default function DistrictSellingChart({ data }) {
  const ref = useRef();

  const categories = ["tops", "bottoms", "shoes", "outerwear", "accessories"];

  useEffect(() => {
    const svg = d3.select(ref.current);
    svg.selectAll("*").remove();

    const width = 700;
    const height = 400;
    const margin = { top: 40, right: 20, bottom: 60, left: 50 };

    svg.attr("width", width).attr("height", height);

    const x0 = d3.scaleBand()
      .domain(data.map(d => d.district))
      .range([margin.left, width - margin.right])
      .paddingInner(0.1);

    const x1 = d3.scaleBand()
      .domain(categories)
      .range([0, x0.bandwidth()])
      .padding(0.05);

    const y = d3.scaleLinear()
      .domain([0, d3.max(data, d => d3.max(categories, key => d[key]))])
      .nice()
      .range([height - margin.bottom, margin.top]);

    // Dark green and peach palette
    const palette = [
      "#225622", // dark green
      "#ffe5b4", // peach
      "#388e3c", // secondary green
      "#ffd9a0", // lighter peach
      "#b7b97a"  // olive
    ];
    const color = d3.scaleOrdinal()
      .domain(categories)
      .range(palette);

    const xAxis = g => g
      .attr("transform", `translate(0,${height - margin.bottom})`)
      .call(d3.axisBottom(x0))
      .selectAll("text")
      .attr("transform", "rotate(-25)")
      .style("text-anchor", "end");

    const yAxis = g => g
      .attr("transform", `translate(${margin.left},0)`)
      .call(d3.axisLeft(y).ticks(null, "s"));

    svg.append("g").call(xAxis);
    svg.append("g").call(yAxis);

    // Tooltip logic
    let tooltip = d3.select("#district-selling-tooltip");
    if (tooltip.empty()) {
      tooltip = d3.select("body")
        .append("div")
        .attr("id", "district-selling-tooltip")
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

    svg.append("g")
      .selectAll("g")
      .data(data)
      .join("g")
      .attr("transform", d => `translate(${x0(d.district)},0)`)
      .selectAll("rect")
      .data(d => categories.map(key => ({ key, value: d[key], district: d.district })))
      .join("rect")
      .attr("x", d => x1(d.key))
      .attr("y", d => y(d.value))
      .attr("width", x1.bandwidth())
      .attr("height", d => y(0) - y(d.value))
      .attr("fill", d => color(d.key))
      .on("mouseover", function (event, d) {
        tooltip
          .style("display", "block")
          .html(`<strong>${d.district}</strong><br/>${d.key}: ${d.value}`);
        d3.select(this).attr("opacity", 0.7);
      })
      .on("mousemove", function (event) {
        tooltip
          .style("left", `${event.clientX + 12}px`)
          .style("top", `${event.clientY - 32}px`);
      })
      .on("mouseout", function () {
        tooltip.style("display", "none");
        d3.select(this).attr("opacity", 1);
      });

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
        Clothing Category Sales by District
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