const CRITERIA = ["CPU", "RAM", "STORAGE", "GPU", "PRICE"];
const CRITERIA_LABELS = {
  CPU: "CPU",
  RAM: "RAM",
  STORAGE: "Storage",
  GPU: "GPU",
  PRICE: "Price",
};
const SERIES_COLORS = ["var(--series-1)", "var(--series-2)", "var(--series-3)", "var(--series-4)", "var(--series-5)"];

// Saaty scale mapped onto a 9-position slider: 0..3 favor the left criterion
// (9,7,5,3), 4 is "equal", 5..8 favor the right criterion (3,5,7,9).
const INTENSITIES = [9, 7, 5, 3, 1, 3, 5, 7, 9];

const pairs = [];
for (let i = 0; i < CRITERIA.length; i++) {
  for (let j = i + 1; j < CRITERIA.length; j++) {
    pairs.push([i, j]);
  }
}

function buildPairwiseUI() {
  const container = document.getElementById("pairwiseContainer");
  pairs.forEach(([i, j], index) => {
    const left = CRITERIA_LABELS[CRITERIA[i]];
    const right = CRITERIA_LABELS[CRITERIA[j]];

    const row = document.createElement("div");
    row.className = "pairwise-row";

    const labels = document.createElement("div");
    labels.className = "pairwise-labels";
    labels.innerHTML = `<span>${left}</span><span>${right}</span>`;

    const slider = document.createElement("input");
    slider.type = "range";
    slider.min = "0";
    slider.max = "8";
    slider.step = "1";
    slider.value = "4";
    slider.dataset.pairIndex = String(index);

    const valueLabel = document.createElement("div");
    valueLabel.className = "pairwise-value";

    const updateLabel = () => {
      const pos = Number(slider.value);
      const intensity = INTENSITIES[pos];
      if (pos === 4) {
        valueLabel.textContent = "Equally important";
      } else if (pos < 4) {
        valueLabel.textContent = `${left} is ${intensity}x more important than ${right}`;
      } else {
        valueLabel.textContent = `${right} is ${intensity}x more important than ${left}`;
      }
    };
    slider.addEventListener("input", updateLabel);
    updateLabel();

    row.append(labels, slider, valueLabel);
    container.appendChild(row);
  });
}

function buildPairwiseMatrix() {
  const n = CRITERIA.length;
  const matrix = Array.from({ length: n }, (_, i) =>
    Array.from({ length: n }, (_, j) => (i === j ? 1 : 0))
  );

  const sliders = document.querySelectorAll('#pairwiseContainer input[type="range"]');
  sliders.forEach((slider) => {
    const index = Number(slider.dataset.pairIndex);
    const [i, j] = pairs[index];
    const pos = Number(slider.value);
    const intensity = INTENSITIES[pos];

    if (pos === 4) {
      matrix[i][j] = 1;
      matrix[j][i] = 1;
    } else if (pos < 4) {
      matrix[i][j] = intensity;
      matrix[j][i] = 1 / intensity;
    } else {
      matrix[i][j] = 1 / intensity;
      matrix[j][i] = intensity;
    }
  });

  return matrix;
}

function readComputer(fieldsetId) {
  const fieldset = document.getElementById(fieldsetId);
  const get = (name) => fieldset.querySelector(`[name="${name}"]`).value;
  return {
    name: get("name"),
    cpuScore: Number(get("cpuScore")),
    ramGB: Number(get("ramGB")),
    storageGB: Number(get("storageGB")),
    gpuScore: Number(get("gpuScore")),
    price: Number(get("price")),
  };
}

function renderBarChart(containerId, items, valueFormatter) {
  const container = document.getElementById(containerId);
  container.innerHTML = "";
  const maxValue = Math.max(...items.map((it) => it.value), 0.0001);

  items.forEach((item, index) => {
    const row = document.createElement("div");
    row.className = "bar-row";

    const label = document.createElement("div");
    label.className = "bar-label";
    label.textContent = item.label;

    const track = document.createElement("div");
    track.className = "bar-track";
    const fill = document.createElement("div");
    fill.className = "bar-fill";
    fill.style.width = `${(item.value / maxValue) * 100}%`;
    fill.style.background = SERIES_COLORS[index % SERIES_COLORS.length];
    track.appendChild(fill);

    const value = document.createElement("div");
    value.className = "bar-value";
    value.textContent = valueFormatter(item.value);

    row.append(label, track, value);
    container.appendChild(row);
  });
}

function renderConsistencyBadge(ahp) {
  const badge = document.getElementById("consistencyBadge");
  badge.hidden = false;
  const crPercent = (ahp.consistencyRatio * 100).toFixed(1);
  if (ahp.consistent) {
    badge.className = "consistency-badge ok";
    badge.textContent = `Consistency ratio (CR): ${crPercent}% — your pairwise comparisons are consistent.`;
  } else {
    badge.className = "consistency-badge warn";
    badge.textContent = `Consistency ratio (CR): ${crPercent}% — CR is above 10%, consider revisiting your comparisons.`;
  }
}

function renderTable(request, response) {
  const tableView = document.getElementById("tableView");
  const criteriaRows = response.criteria
    .map((c, i) => `<tr><td>${CRITERIA_LABELS[c]}</td><td>${(response.ahp.weights[i] * 100).toFixed(1)}%</td></tr>`)
    .join("");

  const scoreRows = response.scores
    .map((s) => `<tr><td>${s.name}</td><td>${s.closenessCoefficient.toFixed(4)}</td><td>${s.rank}</td></tr>`)
    .join("");

  tableView.innerHTML = `
    <h4>AHP Weights</h4>
    <table><thead><tr><th>Criterion</th><th>Weight</th></tr></thead><tbody>${criteriaRows}</tbody></table>
    <h4>TOPSIS Scores</h4>
    <table><thead><tr><th>Computer</th><th>Closeness Coefficient</th><th>Rank</th></tr></thead><tbody>${scoreRows}</tbody></table>
  `;
}

async function handleCompare() {
  const errorMsg = document.getElementById("errorMsg");
  errorMsg.textContent = "";

  const request = {
    computers: [readComputer("computerA"), readComputer("computerB")],
    pairwiseMatrix: buildPairwiseMatrix(),
  };

  const btn = document.getElementById("compareBtn");
  btn.disabled = true;
  btn.textContent = "Calculating...";

  try {
    const res = await fetch("/api/comparisons", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    });

    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      const message = body.error || Object.values(body)[0] || "Request failed";
      throw new Error(message);
    }

    const response = await res.json();

    document.getElementById("resultsSection").hidden = false;
    document.getElementById("winnerBanner").textContent = `Winner: ${response.winner}`;

    renderConsistencyBadge(response.ahp);

    renderBarChart(
      "weightsChart",
      response.criteria.map((c, i) => ({ label: CRITERIA_LABELS[c], value: response.ahp.weights[i] })),
      (v) => `${(v * 100).toFixed(1)}%`
    );

    renderBarChart(
      "scoresChart",
      response.scores
        .slice()
        .sort((a, b) => a.rank - b.rank)
        .map((s) => ({ label: s.name, value: s.closenessCoefficient })),
      (v) => v.toFixed(3)
    );

    renderTable(request, response);
    document.getElementById("resultsSection").scrollIntoView({ behavior: "smooth" });
  } catch (err) {
    errorMsg.textContent = err.message;
  } finally {
    btn.disabled = false;
    btn.textContent = "Compare";
  }
}

document.getElementById("compareBtn").addEventListener("click", handleCompare);
document.getElementById("toggleTableBtn").addEventListener("click", () => {
  const tableView = document.getElementById("tableView");
  tableView.hidden = !tableView.hidden;
  document.getElementById("toggleTableBtn").textContent = tableView.hidden
    ? "Show table view"
    : "Hide table view";
});

buildPairwiseUI();
