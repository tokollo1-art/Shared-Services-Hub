<script>
	/**
	 * @typedef {Object} Props
	 * @property {boolean} open
	 * @property {string} [title]
	 * @property {() => void} [onclose]
	 * @property {import('svelte').Snippet} [children]
	 * @property {import('svelte').Snippet} [footer]
	 */

	/** @type {Props} */
	let {
		open = $bindable(false),
		title = '',
		onclose = () => {},
		children,
		footer
	} = $props();

	function handleClose() {
		open = false;
		onclose();
	}

	function handleKeydown(event) {
		if (event.key === 'Escape' && open) {
			handleClose();
		}
	}
</script>

<svelte:window onkeydown={handleKeydown} />

{#if open}
	<div
		class="fixed inset-0 z-50 overflow-y-auto"
		aria-labelledby="modal-title"
		role="dialog"
		aria-modal="true"
	>
		<!-- Backdrop -->
		<div
			class="fixed inset-0 bg-[#0A1E4A]/60 backdrop-blur-xs transition-opacity animate-in fade-in"
			onclick={handleClose}
			aria-hidden="true"
		></div>

		<!-- Dialog Container -->
		<div class="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
			<div
				class="relative transform overflow-hidden bg-white text-left shadow-2xl transition-all sm:my-8 sm:w-full sm:max-w-lg border border-[#C8D4E8] animate-in zoom-in-95 duration-150"
			>
				<!-- Header -->
				<div class="flex items-center justify-between border-b border-[#C8D4E8] px-6 py-4 bg-[#F8F9FC]">
					<h3 class="font-serif text-xl font-bold text-[#0D1B3E]" id="modal-title">
						{title}
					</h3>
					<button
						type="button"
						class="p-1 text-[#6A84AC] hover:text-[#0D1B3E] hover:bg-[#EFF4FF] focus:outline-none"
						onclick={handleClose}
						aria-label="Close modal"
					>
						<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
						</svg>
					</button>
				</div>

				<!-- Content -->
				<div class="px-6 py-5 bg-white">
					{#if children}
						{@render children()}
					{/if}
				</div>

				<!-- Footer -->
				{#if footer}
					<div class="border-t border-[#C8D4E8] bg-[#F8F9FC] px-6 py-4 flex items-center justify-end gap-3">
						{@render footer()}
					</div>
				{/if}
			</div>
		</div>
	</div>
{/if}
