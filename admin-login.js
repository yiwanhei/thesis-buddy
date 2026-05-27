const inputs = document.querySelectorAll('input');
if (inputs.length >= 2) {
  const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
  setter.call(inputs[0], 'root');
  inputs[0].dispatchEvent(new Event('input', { bubbles: true }));
  setter.call(inputs[1], '123456');
  inputs[1].dispatchEvent(new Event('input', { bubbles: true }));
}
console.log('Filled inputs');
