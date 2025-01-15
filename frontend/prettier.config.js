/** @type {import('prettier').Config} */
module.exports = {
  semi: true,               // Ставить точку с запятой в конце строки
  singleQuote: true,        // Использовать одинарные кавычки вместо двойных
  trailingComma: 'es5',     // Висячая запятая в объектах и массивах (ES5)
  tabWidth: 2,              // Отступ в 2 пробела
  printWidth: 80,           // Максимальная длина строки
  arrowParens: 'always',    // Всегда ставить скобки у стрелочных функций
  bracketSpacing: true,     // Пробелы между фигурными скобками { foo: bar }
  jsxSingleQuote: true,     // Одинарные кавычки в JSX
  endOfLine: 'lf'           // Линейные переводы (LF) для кроссплатформенности
};