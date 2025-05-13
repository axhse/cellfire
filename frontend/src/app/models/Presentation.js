export function capitalizeText(text) {
  return text.charAt(0).toUpperCase() + text.slice(1);
}

export function describeTimePeriod(period, verbose = true) {
  const sign = period < 0 ? -1 : 1;
  period *= sign;
  let description = "";
  let sections = 0;
  const units = [
    [verbose ? "day" : "d", 24 * 60],
    [verbose ? "hour" : "h", 60],
    [verbose ? "minute" : "m", 1],
  ];
  for (const [unitName, unitMin] of units) {
    const unit = unitMin * 60 * 1000;
    if (unit <= period || (sections === 0 && unitName[0] === "m")) {
      const amount = Math.floor(period / unit);
      period -= amount * unit;
      sections += 1;
      description += ` ${amount}${verbose ? " " : ""}${unitName}${verbose && amount !== 1 ? "s" : ""}`;
    }
  }
  return `${sign < 0 ? "-" : "+"}${verbose || sections > 1 ? " " : ""}${description.slice(1)}`;
}

export function formatDate(date) {
  return `${date.getFullYear()}-${as2digits(date.getMonth() + 1)}-${as2digits(date.getDate())} ${as2digits(date.getHours())}:${as2digits(date.getMinutes())}`;
}

function as2digits(unsignedIntNumber) {
  return String(unsignedIntNumber).padStart(2, "0");
}
