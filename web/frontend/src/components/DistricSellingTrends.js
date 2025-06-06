// src/components/DistrictCategoryBarChart.js
import * as d3 from "d3";
import { useEffect, useRef } from "react";

export default function DistrictCategoryBarChart({data}) {
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

    const color = d3.scaleOrdinal()
      .domain(categories)
      .range(d3.schemeTableau10);

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

    const tooltip = d3.select("#tooltip");

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
          .style("left", `${event.offsetX + 20}px`)
          .style("top", `${event.offsetY}px`);
      })
      .on("mouseout", function () {
        tooltip.style("display", "none");
        d3.select(this).attr("opacity", 1);
      });

  }, []);

  return (
    <div style={{ position: "relative", marginTop: "40px" }}>
      <h4 style={{ textAlign: "center" }}>Clothing Category Sales by District</h4>
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
