<script>
	import RoleGuard from '$lib/components/RoleGuard.svelte';
	import { currentUser } from '$lib/stores/auth.js';

	const user = $derived($currentUser);
</script>

<svelte:head>
	<title>System Admin Dashboard | Shared Services Hub</title>
</svelte:head>

<RoleGuard allowedRoles={['ADMIN']}>
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<div class="border border-[#C8D4E8] bg-[#0A1E4A] p-8 text-white shadow-md mb-8">
			<div>
				<div class="inline-block bg-[#7C3FE4] text-white px-2.5 py-0.5 font-mono text-[10px] font-bold uppercase tracking-widest border border-white/20 mb-2">
					SYSTEM GOVERNANCE
				</div>
				<h1 class="font-serif text-3xl sm:text-4xl font-normal text-white">
					Platform Operations &amp; Governance
				</h1>
				<p class="mt-2 text-sm text-[#C8D4E8] max-w-xl font-light font-sans">
					Manage platform users, monitor matching health, and audit Experience Ledger integrity.
				</p>
			</div>

			<div class="mt-8 pt-6 border-t border-[#1C3565] grid grid-cols-2 sm:grid-cols-4 gap-6">
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Total Platform Users</div>
					<div class="font-mono text-2xl sm:text-3xl font-bold mt-1 text-white">1,240</div>
				</div>
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Active SME Tasks</div>
					<div class="font-mono text-2xl sm:text-3xl font-bold mt-1 text-[#1446A0]">184</div>
				</div>
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Verified Ledger Records</div>
					<div class="font-mono text-2xl sm:text-3xl font-bold mt-1 text-[#7C3FE4]">892</div>
				</div>
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Disbursed Corporate Capital</div>
					<div class="font-mono text-2xl sm:text-3xl font-bold mt-1 text-[#1A8A55]">R 1.2M</div>
				</div>
			</div>
		</div>

		<div class="grid grid-cols-1 md:grid-cols-3 gap-6">
			<a
				href="/tasks"
				class="border border-[#C8D4E8] bg-white p-6 shadow-xs hover:border-[#1446A0] transition-colors"
			>
				<h3 class="font-serif text-xl text-[#0D1B3E] mb-1">Task Directory</h3>
				<p class="text-xs text-[#6A84AC] font-sans">Inspect and monitor all active and completed tasks on the platform.</p>
			</a>
			<a
				href="/dashboard/youth"
				class="border border-[#C8D4E8] bg-white p-6 shadow-xs hover:border-[#1446A0] transition-colors"
			>
				<h3 class="font-serif text-xl text-[#0D1B3E] mb-1">Candidate View</h3>
				<p class="text-xs text-[#6A84AC] font-sans">Preview experience ledger and open task matching from the youth perspective.</p>
			</a>
			<a
				href="/dashboard/sme"
				class="border border-[#C8D4E8] bg-white p-6 shadow-xs hover:border-[#1446A0] transition-colors"
			>
				<h3 class="font-serif text-xl text-[#0D1B3E] mb-1">SME Partner View</h3>
				<p class="text-xs text-[#6A84AC] font-sans">Test task publishing, assignment flows, and experience verification.</p>
			</a>
		</div>
	</div>
</RoleGuard>
