// src/components/OutfitTrendChart.js
import * as d3 from "d3";
import { useEffect, useRef } from "react";

export default function OutfitTrendChart({ data }) {
    const ref = useRef();

    useEffect(() => {
        const svg = d3.select(ref.current);
        svg.selectAll("*").remove();

        const width = 600;
        const height = 300;
        const margin = { top: 30, right: 20, bottom: 40, left: 50 };

        svg.attr("width", width).attr("height", height);

        const keys = ['casual', 'formal', 'sport', 'party', 'work', 'other'];
        const stackedData = d3.stack().keys(keys)(data);

        // Use a unique tooltip for this chart
        let tooltip = d3.select("#outfit-trend-tooltip");
        if (tooltip.empty()) {
            tooltip = d3.select("body")
                .append("div")
                .attr("id", "outfit-trend-tooltip")
                .style("position", "fixed")
                .style("background", "white")
                .style("border", "1px solid #ccc")
                .style("padding", "5px 10px")
                .style("border-radius", "4px")
                .style("pointer-events", "none")
                .style("font-size", "12px")
                .style("display", "none")
                .style("box-shadow", "0px 2px 4px rgba(0,0,0,0.2)");
        }

        const x = d3
            .scaleBand()
            .domain(data.map((d) => d.date))
            .range([margin.left, width - margin.right])
            .padding(0.1);

        const y = d3
            .scaleLinear()
            .domain([0, d3.max(data, d => keys.reduce((acc, k) => acc + d[k], 0))])
            .nice()
            .range([height - margin.bottom, margin.top]);

        const color = d3
            .scaleOrdinal()
            .domain(keys)
            .range(["#4CAF50", "#FF9800", "#2196F3", "#9C27B0", "#1976d2", "#607d8b"]);

        // Axes
        svg.append("g")
            .attr("transform", `translate(0,${height - margin.bottom})`)
            .call(d3.axisBottom(x).tickSizeOuter(0))
            .selectAll("text")
            .attr("transform", "rotate(-40)")
            .style("text-anchor", "end");

        svg.append("g")
            .attr("transform", `translate(${margin.left},0)`)
            .call(d3.axisLeft(y));

        // Bars
        svg.append("g")
            .selectAll("g")
            .data(stackedData)
            .join("g")
            .attr("fill", d => color(d.key))
            .selectAll("rect")
            .data(d => d.map(item => ({ ...item, key: d.key })))
            .join("rect")
            .attr("x", d => x(d.data.date))
            .attr("y", d => y(d[1]))
            .attr("height", d => y(d[0]) - y(d[1]))
            .attr("width", x.bandwidth())
            .on("mouseover", function (event, d) {
                tooltip
                    .style("display", "block")
                    .html(`<strong>${d.key}</strong><br/>${d.data.date}: ${d.data[d.key]}`);
            })
            .on("mousemove", function (event) {
                tooltip
                    .style("left", `${event.clientX + 12}px`)
                    .style("top", `${event.clientY - 32}px`);
            })
            .on("mouseout", function () {
                tooltip.style("display", "none");
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
            Outfit Trend Over Time
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